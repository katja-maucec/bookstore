package com.stoecklin.bookstore.web.rest;

import com.stoecklin.bookstore.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ElasticsearchIndexResource {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexResource.class);
    private final BookService bookService;

    public ElasticsearchIndexResource(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/_reindex")
    public ResponseEntity<Void> reindexAll() {
        log.debug("REST request to reindex all data");
        bookService.reindexAllBooks();
        return ResponseEntity.ok().build();
    }
}
