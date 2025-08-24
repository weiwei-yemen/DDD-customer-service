package com.customerservice.ticket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.customerservice.domain.model.valueobject.MessageSource;
import com.customerservice.ticket.domain.model.aggregate.CustomerTicket;
import com.customerservice.ticket.domain.model.valueobject.TicketStatus;
import com.customerservice.ticket.domain.respository.CustomerTicketRepository;
import com.customerservice.ticket.infrastructure.repository.mapper.CustomerTicketMapper;
import com.customerservice.ticket.infrastructure.repository.mapper.MessageMapper;
import com.customerservice.ticket.infrastructure.repository.po.CustomerTicketPO;
import com.customerservice.ticket.infrastructure.repository.po.MessagePO;

//@ExtendWith(SpringExtension.class)

//@SpringBootTest中已经包含了@ExtendWith，所以上面个的注解可以移除了
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CustomerTicketRepositoryTests {

    @MockBean
    private CustomerTicketMapper customerTicketMapper;

    @MockBean
    private MessageMapper messageMapper;

    @Autowired
    private CustomerTicketRepository customerTicketRepository;

    @Test
    public void testFindByTicketId() throws Exception {

        CustomerTicketPO customerTicketPO = buildCustomerTicketPO();
        // 当执行customerTicketMapper.findByTicketId("ticketId1")时，不会真正执行数据库操作，
        // 而是模拟直接返回customerTicketPO
        Mockito.when(customerTicketMapper.findByTicketId("ticketId1")).thenReturn(customerTicketPO);

        List<MessagePO> messagePOs = buildMessagePOs();
        // 同上
        Mockito.when(messageMapper.findByTicketId("ticketId1")).thenReturn(messagePOs);


        /*
            这个测试的目的是在不依赖数据库的情况下，验证 CustomerTicketRepository 是否能正确地从各个 Mapper 获取数据，
            并将它们组装成一个完整的 CustomerTicket 领域对象。通过 Mockito 模拟依赖项（Mappers），测试被隔离，变得快速且稳定。
         */
        CustomerTicket target = this.customerTicketRepository.findByTicketId("ticketId1");
        assertThat(target).isNotNull();
        assertThat(target.getTicketId().getTicketId()).isEqualTo("ticketId1");
        assertThat(target.getMessages().size()).isEqualTo(2);
    }

    private CustomerTicketPO buildCustomerTicketPO() {

        CustomerTicketPO customerTicketPO = new CustomerTicketPO();
        customerTicketPO.setId(1L);
        customerTicketPO.setTicketId("ticketId1");
        customerTicketPO.setStatus(TicketStatus.INITIALIZED);
        customerTicketPO.setScore(0);
        customerTicketPO.setAccount("account1");
        customerTicketPO.setInquire("myInquire");
        customerTicketPO.setOrderNumber("orderNumber1");

        customerTicketPO.setStaffId("staffId1");
        customerTicketPO.setStaffName("staffName1");
        customerTicketPO.setStaffDescription("staffDescription");

        return customerTicketPO;
    }

    private List<MessagePO> buildMessagePOs() {
        List<MessagePO> messagePOs = new ArrayList<>();
        messagePOs.add(buildMessagePO1());
        messagePOs.add(buildMessagePO2());

        return messagePOs;
    }

    private MessagePO buildMessagePO1() {
        MessagePO messagePO = new MessagePO();
        messagePO.setTicketId("ticketId1");
        messagePO.setAccount("account1");
        messagePO.setMessage("message1");
        messagePO.setMessageSource(MessageSource.CUSTOMER);
        messagePO.setStaff("staffId1");

        return messagePO;
    }

    private MessagePO buildMessagePO2() {
        MessagePO messagePO = new MessagePO();
        messagePO.setTicketId("ticketId1");
        messagePO.setAccount("account1");
        messagePO.setMessage("message2");
        messagePO.setMessageSource(MessageSource.STAFF);
        messagePO.setStaff("staffId1");

        return messagePO;
    }
}
