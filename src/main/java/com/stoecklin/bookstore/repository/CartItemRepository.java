package com.stoecklin.bookstore.repository;

import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.domain.CartItem;
import com.stoecklin.bookstore.domain.ShoppingCart;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CartItem entity.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    default Optional<CartItem> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<CartItem> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<CartItem> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select cartItem from CartItem cartItem left join fetch cartItem.book",
        countQuery = "select count(cartItem) from CartItem cartItem"
    )
    Page<CartItem> findAllWithToOneRelationships(Pageable pageable);

    @Query("select cartItem from CartItem cartItem left join fetch cartItem.book")
    List<CartItem> findAllWithToOneRelationships();

    @Query("select cartItem from CartItem cartItem left join fetch cartItem.book where cartItem.id =:id")
    Optional<CartItem> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<CartItem> findByCartAndBook(ShoppingCart cart, Book book);
}
