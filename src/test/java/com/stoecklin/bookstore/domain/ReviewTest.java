package com.stoecklin.bookstore.domain;

import static com.stoecklin.bookstore.domain.BookTestSamples.*;
import static com.stoecklin.bookstore.domain.ReviewTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.stoecklin.bookstore.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReviewTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Review.class);
        Review review1 = getReviewSample1();
        Review review2 = new Review();
        assertThat(review1).isNotEqualTo(review2);

        review2.setId(review1.getId());
        assertThat(review1).isEqualTo(review2);

        review2 = getReviewSample2();
        assertThat(review1).isNotEqualTo(review2);
    }

    @Test
    void bookTest() {
        Review review = getReviewRandomSampleGenerator();
        Book bookBack = getBookRandomSampleGenerator();

        review.setBook(bookBack);
        assertThat(review.getBook()).isEqualTo(bookBack);

        review.book(null);
        assertThat(review.getBook()).isNull();
    }
}
