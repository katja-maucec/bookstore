package com.stoecklin.bookstore.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.repository.BookRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Book} entity.
 */
public interface BookSearchRepository extends ElasticsearchRepository<Book, Long>, BookSearchRepositoryInternal {}

interface BookSearchRepositoryInternal {
    Stream<Book> search(String query);

    Stream<Book> search(Query query);

    @Async
    void index(Book entity);

    @Async
    void deleteFromIndexById(Long id);
}

class BookSearchRepositoryInternalImpl implements BookSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final BookRepository repository;

    BookSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, BookRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Book> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Book> search(Query query) {
        return elasticsearchTemplate.search(query, Book.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Book entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Book.class);
    }
}
