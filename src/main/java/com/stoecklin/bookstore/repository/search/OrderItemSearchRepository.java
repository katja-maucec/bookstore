package com.stoecklin.bookstore.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.stoecklin.bookstore.domain.OrderItem;
import com.stoecklin.bookstore.repository.OrderItemRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link OrderItem} entity.
 */
public interface OrderItemSearchRepository extends ElasticsearchRepository<OrderItem, Long>, OrderItemSearchRepositoryInternal {}

interface OrderItemSearchRepositoryInternal {
    Stream<OrderItem> search(String query);

    Stream<OrderItem> search(Query query);

    @Async
    void index(OrderItem entity);

    @Async
    void deleteFromIndexById(Long id);
}

class OrderItemSearchRepositoryInternalImpl implements OrderItemSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final OrderItemRepository repository;

    OrderItemSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, OrderItemRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<OrderItem> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<OrderItem> search(Query query) {
        return elasticsearchTemplate.search(query, OrderItem.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(OrderItem entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), OrderItem.class);
    }
}
