package com.stoecklin.bookstore.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.stoecklin.bookstore.domain.CartItem;
import com.stoecklin.bookstore.repository.CartItemRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link CartItem} entity.
 */
public interface CartItemSearchRepository extends ElasticsearchRepository<CartItem, Long>, CartItemSearchRepositoryInternal {}

interface CartItemSearchRepositoryInternal {
    Stream<CartItem> search(String query);

    Stream<CartItem> search(Query query);

    @Async
    void index(CartItem entity);

    @Async
    void deleteFromIndexById(Long id);
}

class CartItemSearchRepositoryInternalImpl implements CartItemSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final CartItemRepository repository;

    CartItemSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, CartItemRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<CartItem> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<CartItem> search(Query query) {
        return elasticsearchTemplate.search(query, CartItem.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(CartItem entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), CartItem.class);
    }
}
