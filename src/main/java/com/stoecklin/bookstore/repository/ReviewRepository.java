package com.stoecklin.bookstore.repository;

import com.stoecklin.bookstore.domain.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Review entity.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("select review from Review review where review.user.login = ?#{authentication.name}")
    List<Review> findByUserIsCurrentUser();

    default Optional<Review> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Review> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Review> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    List<Review> findByBook_Id(Long bookId);

    @Query("select review from Review review left join fetch review.user where review.book.id = :bookId")
    List<Review> findByBook_IdWithUser(@Param("bookId") Long bookId);

    @Query(value = "select review from Review review left join fetch review.user", countQuery = "select count(review) from Review review")
    Page<Review> findAllWithToOneRelationships(Pageable pageable);

    @Query("select review from Review review left join fetch review.user")
    List<Review> findAllWithToOneRelationships();

    @Query("select review from Review review left join fetch review.user where review.id =:id")
    Optional<Review> findOneWithToOneRelationships(@Param("id") Long id);
}
