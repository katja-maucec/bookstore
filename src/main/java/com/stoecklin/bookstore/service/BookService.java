package com.stoecklin.bookstore.service;

import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.repository.BookRepository;
import com.stoecklin.bookstore.repository.search.BookSearchRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for managing {@link Book} entities and keeping the search index in sync.
 */
@Service
@Transactional
public class BookService {

    private final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final BookSearchRepository bookSearchRepository;

    public BookService(BookRepository bookRepository, BookSearchRepository bookSearchRepository) {
        this.bookRepository = bookRepository;
        this.bookSearchRepository = bookSearchRepository;
    }

    /**
     * Save (create or update) a book and index it.
     */
    public Book save(Book book) {
        log.debug("Request to save Book : {}", book);
        Book result = bookRepository.save(book);
        try {
            bookSearchRepository.index(result); // async in the generated repo
        } catch (RuntimeException e) {
            log.warn("Failed to index book in Elasticsearch: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Partial update of a book.
     */
    public Optional<Book> partialUpdate(Book book) {
        log.debug("Request to partially update Book : {}", book);

        return bookRepository
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
                // add other fields if your entity has more
                return existingBook;
            })
            .map(bookRepository::save)
            .map(saved -> {
                try {
                    bookSearchRepository.index(saved);
                } catch (RuntimeException e) {
                    log.warn("Failed to index book in Elasticsearch after partial update: {}", e.getMessage());
                }
                return saved;
            });
    }

    /**
     * Get all books. If eagerload is true, returns relationships eagerly (many-to-many).
     */
    @Transactional(readOnly = true)
    public List<Book> findAll(boolean eagerload) {
        log.debug("Request to get all Books (eagerload={})", eagerload);
        if (eagerload) {
            return bookRepository.findAllWithEagerRelationships();
        }
        return bookRepository.findAll();
    }

    /**
     * Get one book by id.
     */
    @Transactional(readOnly = true)
    public Optional<Book> findOne(Long id) {
        log.debug("Request to get Book : {}", id);
        return bookRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the book (DB + search index).
     */
    public void delete(Long id) {
        log.debug("Request to delete Book : {}", id);
        bookRepository.deleteById(id);
        try {
            bookSearchRepository.deleteFromIndexById(id);
        } catch (RuntimeException e) {
            log.warn("Failed to delete book from Elasticsearch index: {}", e.getMessage());
        }
    }

    /**
     * Search for the books matching the query (delegates to the search repository). Returns a Stream so the controller
     * can collect as a List when desired.
     */
    @Transactional(readOnly = true)
    public Stream<Book> search(String query) {
        log.debug("Request to search Books for query {}", query);
        return bookSearchRepository.search(query);
    }

    /**
     * ExistsById helper used by the controller for validation.
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return bookRepository.existsById(id);
    }

    /**
     * Reindex all books from the database into Elasticsearch.
     */
    public void reindexAllBooks() {
        log.debug("Reindexing all books into Elasticsearch...");
        List<Book> books = bookRepository.findAll();
        books.forEach(bookSearchRepository::index);
        log.debug("Reindexed {} books successfully", books.size());
    }
}
