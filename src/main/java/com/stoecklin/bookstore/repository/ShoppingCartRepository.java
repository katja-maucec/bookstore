package com.stoecklin.bookstore.repository;

import com.stoecklin.bookstore.domain.ShoppingCart;
import com.stoecklin.bookstore.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ShoppingCart entity.
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    default Optional<ShoppingCart> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ShoppingCart> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ShoppingCart> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select shoppingCart from ShoppingCart shoppingCart left join fetch shoppingCart.user",
        countQuery = "select count(shoppingCart) from ShoppingCart shoppingCart"
    )
    Page<ShoppingCart> findAllWithToOneRelationships(Pageable pageable);

    @Query("select shoppingCart from ShoppingCart shoppingCart left join fetch shoppingCart.user")
    List<ShoppingCart> findAllWithToOneRelationships();

    @Query("select shoppingCart from ShoppingCart shoppingCart left join fetch shoppingCart.user where shoppingCart.id " + "=:id")
    Optional<ShoppingCart> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<ShoppingCart> findOneByUserAndCompletedIsFalse(User user);

    Optional<ShoppingCart> findByUserAndCompleted(User user, Boolean completed);

    @Query(
        "SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.items items LEFT JOIN FETCH items.book WHERE sc.user.id = " +
        ":userId AND sc.completed = :completed"
    )
    Optional<ShoppingCart> findByUserAndCompletedWithItems(@Param("userId") Long userId, @Param("completed") Boolean completed);
}
