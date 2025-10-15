package com.stoecklin.bookstore.config;

import com.stoecklin.bookstore.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchInitializer implements CommandLineRunner {

    private final BookService bookService;

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchInitializer.class);

    public ElasticsearchInitializer(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) {
        LOG.debug("Reindexing all books into Elasticsearch...");
        bookService.reindexAllBooks();
        LOG.debug("Reindexing complete!");
    }
}
