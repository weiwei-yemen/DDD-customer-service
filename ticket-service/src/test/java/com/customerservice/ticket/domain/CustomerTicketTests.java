package com.customerservice.ticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.customerservice.domain.model.entity.OrderProfile;
import com.customerservice.domain.model.entity.StaffProfile;
import com.customerservice.domain.model.valueobject.GoodsProfile;
import com.customerservice.domain.model.valueobject.MessageSource;
import com.customerservice.ticket.domain.command.ApplyTicketCommand;
import com.customerservice.ticket.domain.command.FinishTicketCommand;
import com.customerservice.ticket.domain.command.ProcessTicketCommand;
import com.customerservice.ticket.domain.model.aggregate.CustomerTicket;
import com.customerservice.ticket.domain.model.valueobject.TicketStatus;


@ExtendWith(SpringExtension.class)
public class CustomerTicketTests {

	@Test
	public void testCustomerTicketCreation() throws Exception {		
		CustomerTicket customerTicket = initCustomerTicket();

		assertThat(customerTicket.getTicketId().getTicketId()).isNotNull();
		assertThat(customerTicket.getStatus()).isEqualTo(TicketStatus.INITIALIZED);
		assertThat(customerTicket.getScore().getScore()).isEqualTo(0);
	}	

	@Test
	public void testCustomerTicketFinishing() throws Exception {		
		CustomerTicket customerTicket = initCustomerTicket();
		
		FinishTicketCommand finishTicketCommand = new FinishTicketCommand("ticketId1", "ticket_is_finished", 100);
				
		customerTicket.finishTicket(finishTicketCommand.getTicketId(), finishTicketCommand.getMessage(), finishTicketCommand.getScore());
		assertThat(customerTicket.getStatus()).isEqualTo(TicketStatus.CLOSED);	
		assertThat(customerTicket.getScore().getScore()).isEqualTo(100);	
	}
	
	//初始化一个CustomerTicket
	private CustomerTicket initCustomerTicket() {
		
		String account = "tianyalan";
		String inquire = "myInquire";
		
		OrderProfile order = new OrderProfile();
		order.setOrderNumber("orderNumber1");
		List<GoodsProfile> goodsList = new ArrayList<GoodsProfile>();
		goodsList.add(new GoodsProfile("goodsCode1", "goodsName1", 100F));
		order.setGoodsList(goodsList);
		order.setDeliveryAddress("deliveryAddress1");
		
		StaffProfile staff = new StaffProfile("staff1", "staffname1", "description1");
		
		CustomerTicket customerTicket = new CustomerTicket(account, inquire, order, staff);
		
		return customerTicket;
	}
}
