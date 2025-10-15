package com.stoecklin.bookstore.domain;

import static com.stoecklin.bookstore.domain.BookTestSamples.*;
import static com.stoecklin.bookstore.domain.CartItemTestSamples.*;
import static com.stoecklin.bookstore.domain.ShoppingCartTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.stoecklin.bookstore.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CartItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CartItem.class);
        CartItem cartItem1 = getCartItemSample1();
        CartItem cartItem2 = new CartItem();
        assertThat(cartItem1).isNotEqualTo(cartItem2);

        cartItem2.setId(cartItem1.getId());
        assertThat(cartItem1).isEqualTo(cartItem2);

        cartItem2 = getCartItemSample2();
        assertThat(cartItem1).isNotEqualTo(cartItem2);
    }

    @Test
    void bookTest() {
        CartItem cartItem = getCartItemRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        cartItem.setBook(bookBack);
        assertThat(cartItem.getBook()).isEqualTo(bookBack);

        cartItem.book(null);
        assertThat(cartItem.getBook()).isNull();
    }

    @Test
    void cartTest() {
        CartItem cartItem = getCartItemRandomSampleGenerator();
        ShoppingCart shoppingCartBack = getShoppingCartRandomSampleGenerator();

        cartItem.setCart(shoppingCartBack);
        assertThat(cartItem.getCart()).isEqualTo(shoppingCartBack);

        cartItem.cart(null);
        assertThat(cartItem.getCart()).isNull();
    }
}
