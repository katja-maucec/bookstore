package com.stoecklin.bookstore.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.stoecklin.bookstore.domain.Category;
import com.stoecklin.bookstore.repository.CategoryRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Category} entity.
 */
public interface CategorySearchRepository extends ElasticsearchRepository<Category, Long>, CategorySearchRepositoryInternal {}

interface CategorySearchRepositoryInternal {
    Stream<Category> search(String query);

    Stream<Category> search(Query query);

    @Async
    void index(Category entity);

    @Async
    void deleteFromIndexById(Long id);
}

class CategorySearchRepositoryInternalImpl implements CategorySearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final CategoryRepository repository;

    CategorySearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, CategoryRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Category> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Category> search(Query query) {
        return elasticsearchTemplate.search(query, Category.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Category entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Category.class);
    }
}
