package com.customerservice.ticket.domain.model.aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.customerservice.domain.model.entity.OrderProfile;
import org.springframework.data.domain.AbstractAggregateRoot;

import com.customerservice.domain.event.TicketAppliedEvent;
import com.customerservice.domain.model.entity.StaffProfile;
import com.customerservice.domain.model.valueobject.MessageSource;
import com.customerservice.ticket.domain.command.ApplyTicketCommand;
import com.customerservice.ticket.domain.command.FinishTicketCommand;
import com.customerservice.ticket.domain.command.ProcessTicketCommand;
import com.customerservice.ticket.domain.model.entity.Consultation;
import com.customerservice.ticket.domain.model.valueobject.Message;
import com.customerservice.ticket.domain.model.valueobject.TicketScore;
import com.customerservice.ticket.domain.model.valueobject.TicketStatus;

public class CustomerTicket extends AbstractAggregateRoot<CustomerTicket> {

	private TicketId ticketId;// 客服工单唯一编号
	private Consultation consultation;// 用户咨询
	private StaffProfile staff;// 客服人员
	private TicketStatus status;// 工单状态
	private List<Message> messages;// 工单详细消息列表
	private Message latestMessage;// 工单最近一条消息
	private TicketScore score;// 工单评分
	
	public CustomerTicket() {

	}

	public CustomerTicket(String account, String inquire, OrderProfile order, StaffProfile staff) {
		// 1.设置聚合标识符
		this.ticketId = new TicketId("Ticket" + UUID.randomUUID().toString().toUpperCase());

		// 2.创建Consultation
		String consultationId = "Consultation" + UUID.randomUUID().toString().toUpperCase();
		this.consultation = new Consultation(consultationId, account, order, inquire);
		
		// 3.获取客服信息
		this.staff = staff;
		
		// 4.初始化基础信息
		this.status = TicketStatus.INITIALIZED;
		this.messages = new ArrayList<Message>();
		this.score = new TicketScore(0);
		
		// 5.发布工单申请事件
		TicketAppliedEvent ticketAppliedEvent = new TicketAppliedEvent(
				this.ticketId.getTicketId(), 
				account, 
				staff.getStaffName(), 
				MessageSource.CUSTOMER, 
				inquire);
		this.registerEvent(ticketAppliedEvent);
	}
		
	public void processTicket(ProcessTicketCommand processTicketCommand) {
		
		// 验证TicketId是否对当前CustomerTicket对象是否有效
		String ticketId = processTicketCommand.getTicketId();
		if(!ticketId.equals(this.ticketId.getTicketId())) {
			return;
		}

		// 构建Message
		Message message = new Message(processTicketCommand.getTicketId(), this.consultation.getAccount(),
				this.staff.getStaffName(), processTicketCommand.getMessageSource(), processTicketCommand.getMessage());
		latestMessage = message;
		this.messages.add(message);

		// 更新工单状态
		this.status = TicketStatus.INPROCESS;
	}

	public void finishTicket(FinishTicketCommand finishTicketCommand) {
		// 构建Message
		Message message = new Message(finishTicketCommand.getTicketId(), this.consultation.getAccount(),
				this.staff.getStaffName(), MessageSource.CUSTOMER, finishTicketCommand.getMessage());
		latestMessage = message;
		this.messages.add(message);
		
		// 更新工单状态
		this.status = TicketStatus.CLOSED;

		// 设置工单评分
		this.score = new TicketScore(finishTicketCommand.getScore());
	}

	public TicketId getTicketId() {
		return ticketId;
	}
	public void setTicketId(TicketId ticketId) {
		this.ticketId = ticketId;
	}
	public Consultation getConsultation() {
		return consultation;
	}
	public void setConsultation(Consultation consultation) {
		this.consultation = consultation;
	}
	public StaffProfile getStaff() {
		return staff;
	}
	public void setStaff(StaffProfile staff) {
		this.staff = staff;
	}
	public TicketStatus getStatus() {
		return status;
	}
	public void setStatus(TicketStatus status) {
		this.status = status;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	public Message getLatestMessage() {
		return latestMessage;
	}
	public void setLatestMessage(Message latestMessage) {
		this.latestMessage = latestMessage;
	}
	public TicketScore getScore() {
		return score;
	}
	public void setScore(TicketScore score) {
		this.score = score;
	}
	
	/**
	 * 获取未提交的领域事件
	 * 这个方法使存储库可以访问事件而无需使用反射
	 * @return 领域事件列表
	 */
	public List<Object> getUncommittedEvents() {
		return new ArrayList<>(this.domainEvents());
	}
	
	/**
	 * 清理已提交的事件
	 */
	public void markEventsAsCommitted() {
		this.clearDomainEvents();
	}
}
