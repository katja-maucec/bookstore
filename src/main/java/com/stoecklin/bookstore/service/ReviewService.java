package com.stoecklin.bookstore.service;

import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.domain.Review;
import com.stoecklin.bookstore.repository.BookRepository;
import com.stoecklin.bookstore.repository.ReviewRepository;
import com.stoecklin.bookstore.repository.search.ReviewSearchRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ReviewService {

    private final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final ReviewSearchRepository reviewSearchRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository, ReviewSearchRepository reviewSearchRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.reviewSearchRepository = reviewSearchRepository;
    }

    public Review save(Review review) {
        log.debug("Request to save Review : {}", review);

        // ensure timestamp
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(Instant.now());
        }

        Review result = reviewRepository.save(review);
        reviewSearchRepository.index(result);

        // recalculate average rating for the associated book
        if (review.getBook() != null && review.getBook().getId() != null) {
            Book book = bookRepository.findById(review.getBook().getId()).orElse(null);
            if (book != null) {
                review.setBook(book); // ensures the book reference is managed
                reviewRepository.save(review);
                updateBookAverageRating(book.getId());
            }
        }

        return result;
    }

    private void updateBookAverageRating(Long bookId) {
        List<Review> reviews = reviewRepository
            .findAll()
            .stream()
            .filter(r -> r.getBook() != null && r.getBook().getId().equals(bookId))
            .toList();

        if (!reviews.isEmpty()) {
            DoubleSummaryStatistics stats = reviews.stream().mapToDouble(Review::getRating).summaryStatistics();

            double average = stats.getAverage();

            Book book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                book.setAverageRating(average);
                bookRepository.save(book);
                log.debug("Updated average rating for book {} to {}", bookId, average);
            }
        }
    }

    @Transactional
    public void recalculateBookAverageRating(Long bookId) {
        updateBookAverageRating(bookId);
    }

    public List<Review> findByBook(Long bookId) {
        log.debug("Request to get all Reviews for Book ID : {}", bookId);
        return reviewRepository.findByBook_Id(bookId);
    }
}
