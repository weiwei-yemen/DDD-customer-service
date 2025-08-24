package com.customerservice.ticket.domain.command;

/**
 * 结束工单命令 - 不可变对象
 * 符合DDD架构中Command对象只读的设计原则
 */
public class FinishTicketCommand {

	private final String ticketId;
	private final String message;
	private final Integer score;
	
	public FinishTicketCommand(String ticketId, String message, Integer score) {
		this.ticketId = ticketId;
		this.message = message;
		this.score = score;
	}
	
	public String getTicketId() {
		return ticketId;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Integer getScore() {
		return score;
	}
}
