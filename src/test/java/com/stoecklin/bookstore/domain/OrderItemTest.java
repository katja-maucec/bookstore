package com.stoecklin.bookstore.domain;

import static com.stoecklin.bookstore.domain.BookTestSamples.*;
import static com.stoecklin.bookstore.domain.OrderItemTestSamples.*;
import static com.stoecklin.bookstore.domain.OrderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.stoecklin.bookstore.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OrderItem.class);
        OrderItem orderItem1 = getOrderItemSample1();
        OrderItem orderItem2 = new OrderItem();
        assertThat(orderItem1).isNotEqualTo(orderItem2);

        orderItem2.setId(orderItem1.getId());
        assertThat(orderItem1).isEqualTo(orderItem2);

        orderItem2 = getOrderItemSample2();
        assertThat(orderItem1).isNotEqualTo(orderItem2);
    }

    @Test
    void bookTest() {
        OrderItem orderItem = getOrderItemRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        orderItem.setBook(bookBack);
        assertThat(orderItem.getBook()).isEqualTo(bookBack);

        orderItem.book(null);
        assertThat(orderItem.getBook()).isNull();
    }

    @Test
    void orderTest() {
        OrderItem orderItem = getOrderItemRandomSampleGenerator();
        Order orderBack = getOrderRandomSampleGenerator();

        orderItem.setOrder(orderBack);
        assertThat(orderItem.getOrder()).isEqualTo(orderBack);

        orderItem.order(null);
        assertThat(orderItem.getOrder()).isNull();
    }
}
