package com.customerservice.ticket.application.commandservice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.customerservice.domain.model.entity.OrderProfile;
import com.customerservice.domain.model.entity.StaffProfile;
import com.customerservice.domain.model.valueobject.GoodsProfile;
import com.customerservice.ticket.domain.command.ApplyTicketCommand;
import com.customerservice.ticket.domain.command.FinishTicketCommand;
import com.customerservice.ticket.domain.command.ProcessTicketCommand;
import com.customerservice.ticket.domain.model.aggregate.CustomerTicket;
import com.customerservice.ticket.domain.model.aggregate.TicketId;
import com.customerservice.ticket.domain.respository.CustomerTicketRepository;
import com.customerservice.ticket.infrastructure.mask.MaskService;
import com.customerservice.ticket.integration.acl.AclOrderService;
import com.customerservice.ticket.integration.acl.AclStaffService;

@Service
public class CustomerTicketCommandService {

	private CustomerTicketRepository customerTicketRepository;
	private AclOrderService aclOrderService;
	private AclStaffService aclStaffService;
	private ApplicationEventPublisher eventPublisher;

	public CustomerTicketCommandService(CustomerTicketRepository customerTicketRepository,
			AclOrderService aclOrderService, AclStaffService aclStaffService,
			ApplicationEventPublisher eventPublisher) {

		this.customerTicketRepository = customerTicketRepository;
		this.aclOrderService = aclOrderService;
		this.aclStaffService = aclStaffService;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public TicketId handleCustomerTicketApplication(ApplyTicketCommand applyTicketCommand) {
		// 1. 通过防腐层(ACL)从外部限界上下文获取所需信息
//		OrderProfile order = aclOrderService.getOrderInfo(applyTicketCommand.getOrderNumber());

        // mock
        OrderProfile order = new OrderProfile();
        order.setOrderNumber("orderNumber1");
        List<GoodsProfile> goodsList = new ArrayList<GoodsProfile>();
        goodsList.add(new GoodsProfile("goodsCode1", "goodsName1", 100F));
        order.setGoodsList(goodsList);
        order.setDeliveryAddress("deliveryAddress1");
//        applyTicketCommand.setOrder(order);


//		StaffProfile staff = aclStaffService.getOptimalStaff(applyTicketCommand.getAccount(),
//				order, applyTicketCommand.getInquire());

        // mock
        StaffProfile staff = new StaffProfile("staff1", "staffname1", "description1");

		// 2. 创建CustomerTicket聚合根，将从Command中解包出的原始数据和获取到的外部信息一并传入
		CustomerTicket customerTicket = new CustomerTicket(applyTicketCommand.getAccount(),
				applyTicketCommand.getInquire(), order, staff);

		// 3. 通过资源库持久化CustomerTicket
		customerTicketRepository.save(customerTicket);
		
		// 4. 发布领域事件
		publishDomainEvents(customerTicket);

		// 5. 返回CustomerTicket的聚合标识符
		return customerTicket.getTicketId();
	}

	public void handleCustomerTicketProcessing(ProcessTicketCommand processTicketCommand) {
		// 根据TicketId获取CustomerTicket
		CustomerTicket customerTicket = customerTicketRepository.findByTicketId(processTicketCommand.getTicketId());

		// 调用基础设施层的MaskService进行脱敏
		String message = processTicketCommand.getMessage();
		processTicketCommand.setMessage(MaskService.performMask(message));

		// 处理更新CustomerTicket业务逻辑
		customerTicket.processTicket(processTicketCommand);

		// 通过资源库更新CustomerTicket
		customerTicketRepository.updateCustomerTicket(customerTicket);
	}

	public void handleCustomerTicketFinishing(FinishTicketCommand finishTicketCommand) {
		// 根据TicketId获取CustomerTicket
		CustomerTicket customerTicket = customerTicketRepository.findByTicketId(finishTicketCommand.getTicketId());

		// 处理结束CustomerTicket业务逻辑
		customerTicket.finishTicket(finishTicketCommand);

		// 通过资源库更新CustomerTicket
		customerTicketRepository.updateCustomerTicket(customerTicket);
	}
	
	/**
	 * 发布领域事件
	 * 在应用服务层处理事件发布，确保事务边界清晰
	 */
	private void publishDomainEvents(CustomerTicket customerTicket) {
		List<Object> events = customerTicket.getUncommittedEvents();
		for (Object event : events) {
			eventPublisher.publishEvent(event);
		}
		customerTicket.markEventsAsCommitted();
	}
}
