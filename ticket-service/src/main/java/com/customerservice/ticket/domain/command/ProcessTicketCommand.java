package com.customerservice.ticket.domain.command;

import com.customerservice.domain.model.valueobject.MessageSource;

/**
 * 处理工单命令 - 不可变对象
 * 符合DDD架构中Command对象只读的设计原则
 */
public class ProcessTicketCommand {

	private final String ticketId;
	private final MessageSource messageSource;
	private final String message;
	
	public ProcessTicketCommand(String ticketId, MessageSource messageSource, String message) {
		this.ticketId = ticketId;
		this.messageSource = messageSource;
		this.message = message;
	}

	public String getTicketId() {
		return ticketId;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public String getMessage() {
		return message;
	}
}
