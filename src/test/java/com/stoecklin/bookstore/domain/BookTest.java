package com.stoecklin.bookstore.domain;

import static com.stoecklin.bookstore.domain.BookTestSamples.*;
import static com.stoecklin.bookstore.domain.CategoryTestSamples.*;
import static com.stoecklin.bookstore.domain.ReviewTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.stoecklin.bookstore.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Book.class);
        Book book1 = getBookSample1();
        Book book2 = new Book();
        assertThat(book1).isNotEqualTo(book2);

        book2.setId(book1.getId());
        assertThat(book1).isEqualTo(book2);

        book2 = getBookSample2();
        assertThat(book1).isNotEqualTo(book2);
    }

    @Test
    void reviewTest() {
        Book book = getBookRandomSampleGenerator();
        Review reviewBack = getReviewRandomSampleGenerator();

        book.addReview(reviewBack);
        assertThat(book.getReviews()).containsOnly(reviewBack);
        assertThat(reviewBack.getBook()).isEqualTo(book);

        book.removeReview(reviewBack);
        assertThat(book.getReviews()).doesNotContain(reviewBack);
        assertThat(reviewBack.getBook()).isNull();

        book.reviews(new HashSet<>(Set.of(reviewBack)));
        assertThat(book.getReviews()).containsOnly(reviewBack);
        assertThat(reviewBack.getBook()).isEqualTo(book);

        book.setReviews(new HashSet<>());
        assertThat(book.getReviews()).doesNotContain(reviewBack);
        assertThat(reviewBack.getBook()).isNull();
    }

    @Test
    void categoryTest() {
        Book book = getBookRandomSampleGenerator();
        Category categoryBack = getCategoryRandomSampleGenerator();

        book.setCategory(categoryBack);
        assertThat(book.getCategory()).isEqualTo(categoryBack);

        book.category(null);
        assertThat(book.getCategory()).isNull();
    }
}
