package com.stoecklin.bookstore.repository;

import com.stoecklin.bookstore.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select jhiOrder from Order jhiOrder where jhiOrder.user.login = ?#{authentication.name}")
    List<Order> findByUserIsCurrentUser();

    @Query("select distinct o from Order o left join fetch o.items i left join fetch i.book left join fetch o.user where" + " o.id = :id")
    Optional<Order> findOneWithEagerRelationships(@Param("id") Long id);

    default List<Order> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Order> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select jhiOrder from Order jhiOrder left join fetch jhiOrder.user",
        countQuery = "select count(jhiOrder) from Order jhiOrder"
    )
    Page<Order> findAllWithToOneRelationships(Pageable pageable);

    @Query("select jhiOrder from Order jhiOrder left join fetch jhiOrder.user")
    List<Order> findAllWithToOneRelationships();

    @Query("select jhiOrder from Order jhiOrder left join fetch jhiOrder.user where jhiOrder.id =:id")
    Optional<Order> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndBooks(@Param("id") Long id);

    @Query("select distinct o from Order o left join fetch o.items i left join fetch i.book left join fetch o.user")
    List<Order> findAllWithItemsAndBooks();
}
