package com.stoecklin.bookstore.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.stoecklin.bookstore.domain.Order;
import com.stoecklin.bookstore.repository.OrderRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Order} entity.
 */
public interface OrderSearchRepository extends ElasticsearchRepository<Order, Long>, OrderSearchRepositoryInternal {}

interface OrderSearchRepositoryInternal {
    Stream<Order> search(String query);

    Stream<Order> search(Query query);

    @Async
    void index(Order entity);

    @Async
    void deleteFromIndexById(Long id);
}

class OrderSearchRepositoryInternalImpl implements OrderSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final OrderRepository repository;

    OrderSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, OrderRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Order> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Order> search(Query query) {
        return elasticsearchTemplate.search(query, Order.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Order entity) {
        repository.findOneWithEagerRelationships(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Order.class);
    }
}
