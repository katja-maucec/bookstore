package com.stoecklin.bookstore.config;

import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.domain.Review;
import com.stoecklin.bookstore.repository.BookRepository;
import com.stoecklin.bookstore.repository.ReviewRepository;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AverageRatingBackfillRunner implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(AverageRatingBackfillRunner.class);

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public AverageRatingBackfillRunner(BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Starting backfill of average ratings for all books...");

        List<Book> books = bookRepository.findAll();

        for (Book book : books) {
            List<Review> reviews = reviewRepository.findByBook_Id(book.getId());
            if (!reviews.isEmpty()) {
                DoubleSummaryStatistics stats = reviews.stream().mapToDouble(Review::getRating).summaryStatistics();
                double average = stats.getAverage();
                book.setAverageRating(average);
                bookRepository.save(book);
                log.info("Book '{}' (id={}) updated with averageRating={}", book.getTitle(), book.getId(), average);
            } else {
                log.info("Book '{}' (id={}) has no reviews, skipping", book.getTitle(), book.getId());
            }
        }

        log.info("Average rating backfill complete!");
    }
}
