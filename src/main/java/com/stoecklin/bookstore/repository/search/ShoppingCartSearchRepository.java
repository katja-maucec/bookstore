package com.stoecklin.bookstore.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.stoecklin.bookstore.domain.ShoppingCart;
import com.stoecklin.bookstore.repository.ShoppingCartRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link ShoppingCart} entity.
 */
public interface ShoppingCartSearchRepository extends ElasticsearchRepository<ShoppingCart, Long>, ShoppingCartSearchRepositoryInternal {}

interface ShoppingCartSearchRepositoryInternal {
    Stream<ShoppingCart> search(String query);

    Stream<ShoppingCart> search(Query query);

    @Async
    void index(ShoppingCart entity);

    @Async
    void deleteFromIndexById(Long id);
}

class ShoppingCartSearchRepositoryInternalImpl implements ShoppingCartSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ShoppingCartRepository repository;

    ShoppingCartSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, ShoppingCartRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<ShoppingCart> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<ShoppingCart> search(Query query) {
        return elasticsearchTemplate.search(query, ShoppingCart.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(ShoppingCart entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), ShoppingCart.class);
    }
}
