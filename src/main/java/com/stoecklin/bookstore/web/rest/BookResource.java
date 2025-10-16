package com.stoecklin.bookstore.web.rest;

import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.repository.BookRepository;
import com.stoecklin.bookstore.repository.search.BookSearchRepository;
import com.stoecklin.bookstore.service.BookService;
import com.stoecklin.bookstore.web.rest.errors.BadRequestAlertException;
import com.stoecklin.bookstore.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.stoecklin.bookstore.domain.Book}.
 */
@RestController
@RequestMapping("/api/books")
@Transactional
public class BookResource {

    private static final Logger LOG = LoggerFactory.getLogger(BookResource.class);

    private static final String ENTITY_NAME = "book";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BookRepository bookRepository;

    private final BookSearchRepository bookSearchRepository;

    private final BookService bookService;

    public BookResource(BookRepository bookRepository, BookSearchRepository bookSearchRepository, BookService bookService) {
        this.bookRepository = bookRepository;
        this.bookSearchRepository = bookSearchRepository;
        this.bookService = bookService;
    }

    /**
     * {@code POST  /books} : Create a new book.
     *
     * @param book the book to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new book, or with status
     *     {@code 400 (Bad Request)} if the book has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) throws URISyntaxException {
        LOG.debug("REST request to save Book : {}", book);
        if (book.getId() != null) {
            throw new BadRequestAlertException("A new book cannot already have an ID", ENTITY_NAME, "idexists");
        }
        book = bookRepository.save(book);
        bookSearchRepository.index(book);
        return ResponseEntity.created(new URI("/api/books/" + book.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, book.getId().toString()))
            .body(book);
    }

    /**
     * {@code PUT  /books/:id} : Updates an existing book.
     *
     * @param id the id of the book to save.
     * @param book the book to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated book, or with status
     *     {@code 400 (Bad Request)} if the book is not valid, or with status {@code 500 (Internal Server Error)} if the
     *     book couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody Book book)
        throws URISyntaxException {
        LOG.debug("REST request to update Book : {}, {}", id, book);
        if (book.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, book.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bookRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        book = bookRepository.save(book);
        bookSearchRepository.index(book);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, book.getId().toString()))
            .body(book);
    }

    /**
     * {@code PATCH  /books/:id} : Partial updates given fields of an existing book, field will ignore if it is null
     *
     * @param id the id of the book to save.
     * @param book the book to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated book, or with status
     *     {@code 400 (Bad Request)} if the book is not valid, or with status {@code 404 (Not Found)} if the book is not
     *     found, or with status {@code 500 (Internal Server Error)} if the book couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Book> partialUpdateBook(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Book book
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Book partially : {}, {}", id, book);
        if (book.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, book.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bookRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Book> result = bookRepository
            .findById(book.getId())
            .map(existingBook -> {
                if (book.getTitle() != null) {
                    existingBook.setTitle(book.getTitle());
                }
                if (book.getAuthor() != null) {
                    existingBook.setAuthor(book.getAuthor());
                }
                if (book.getDescription() != null) {
                    existingBook.setDescription(book.getDescription());
                }
                if (book.getPrice() != null) {
                    existingBook.setPrice(book.getPrice());
                }
                if (book.getStock() != null) {
                    existingBook.setStock(book.getStock());
                }

                return existingBook;
            })
            .map(bookRepository::save)
            .map(savedBook -> {
                bookSearchRepository.index(savedBook);
                return savedBook;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, book.getId().toString())
        );
    }

    /**
     * {@code GET  /books} : get all the books.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of books in body.
     */
    @GetMapping("")
    public List<Book> getAllBooks(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Books");
        if (eagerload) {
            return bookRepository.findAllWithEagerRelationships();
        } else {
            return bookRepository.findAll();
        }
    }

    /**
     * {@code GET  /books/:id} : get the "id" book.
     *
     * @param id the id of the book to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the book, or with status
     *     {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Book : {}", id);
        Optional<Book> book = bookRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(book);
    }

    /**
     * {@code DELETE  /books/:id} : delete the "id" book.
     *
     * @param id the id of the book to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Book : {}", id);
        bookRepository.deleteById(id);
        bookSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /books/_search?query=:query} : search for the book corresponding to the query.
     *
     * @param query the query of the book search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Book> searchBooks(@RequestParam("query") String query) {
        LOG.debug("REST request to search Books for query {}", query);
        try {
            return StreamSupport.stream(bookSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }

    @PostMapping("/reindex")
    public ResponseEntity<Void> reindexBooks() {
        LOG.debug("REST request to reindex all Books");
        bookService.reindexAllBooks();
        return ResponseEntity.noContent().build();
    }
}
