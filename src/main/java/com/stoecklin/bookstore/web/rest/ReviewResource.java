package com.stoecklin.bookstore.web.rest;

import com.stoecklin.bookstore.domain.Review;
import com.stoecklin.bookstore.repository.ReviewRepository;
import com.stoecklin.bookstore.repository.search.ReviewSearchRepository;
import com.stoecklin.bookstore.service.ReviewService;
import com.stoecklin.bookstore.web.rest.errors.BadRequestAlertException;
import com.stoecklin.bookstore.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.stoecklin.bookstore.domain.Review}.
 */
@RestController
@RequestMapping("/api/reviews")
@Transactional
public class ReviewResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewResource.class);

    private static final String ENTITY_NAME = "review";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReviewRepository reviewRepository;

    private final ReviewSearchRepository reviewSearchRepository;

    private final ReviewService reviewService;

    public ReviewResource(ReviewRepository reviewRepository, ReviewSearchRepository reviewSearchRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewSearchRepository = reviewSearchRepository;
        this.reviewService = reviewService;
    }

    /**
     * {@code POST  /reviews} : Create a new review.
     *
     * @param review the review to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new review, or with status
     *     {@code 400 (Bad Request)} if the review has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) throws URISyntaxException {
        LOG.debug("REST request to save Review : {}", review);
        if (review.getId() != null) {
            throw new BadRequestAlertException("A new review cannot already have an ID", ENTITY_NAME, "idexists");
        }
        review = reviewService.save(review);

        return ResponseEntity.created(new URI("/api/reviews/" + review.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, review.getId().toString()))
            .body(review);
    }

    /**
     * {@code PUT  /reviews/:id} : Updates an existing review.
     *
     * @param id the id of the review to save.
     * @param review the review to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated review, or with status
     *     {@code 400 (Bad Request)} if the review is not valid, or with status {@code 500 (Internal Server Error)} if
     *     the review couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Review review
    ) throws URISyntaxException {
        LOG.debug("REST request to update Review : {}, {}", id, review);
        if (review.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, review.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reviewRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        review = reviewRepository.save(review);
        reviewSearchRepository.index(review);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, review.getId().toString()))
            .body(review);
    }

    /**
     * {@code PATCH  /reviews/:id} : Partial updates given fields of an existing review, field will ignore if it is
     * null
     *
     * @param id the id of the review to save.
     * @param review the review to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated review, or with status
     *     {@code 400 (Bad Request)} if the review is not valid, or with status {@code 404 (Not Found)} if the review is
     *     not found, or with status {@code 500 (Internal Server Error)} if the review couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Review> partialUpdateReview(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Review review
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Review partially : {}, {}", id, review);
        if (review.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, review.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reviewRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Review> result = reviewRepository
            .findById(review.getId())
            .map(existingReview -> {
                if (review.getRating() != null) {
                    existingReview.setRating(review.getRating());
                }
                if (review.getComment() != null) {
                    existingReview.setComment(review.getComment());
                }
                if (review.getCreatedAt() != null) {
                    existingReview.setCreatedAt(review.getCreatedAt());
                }

                return existingReview;
            })
            .map(reviewRepository::save)
            .map(savedReview -> {
                reviewSearchRepository.index(savedReview);
                return savedReview;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, review.getId().toString())
        );
    }

    /**
     * {@code GET  /reviews} : get all the reviews.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reviews in body.
     */
    @GetMapping("")
    public List<Review> getAllReviews(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Reviews");
        if (eagerload) {
            return reviewRepository.findAllWithEagerRelationships();
        } else {
            return reviewRepository.findAll();
        }
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByBook(@PathVariable Long bookId) {
        List<Review> reviews = reviewService.findByBook(bookId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * {@code GET  /reviews/:id} : get the "id" review.
     *
     * @param id the id of the review to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the review, or with status
     *     {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReview(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Review : {}", id);
        Optional<Review> review = reviewRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(review);
    }

    /**
     * {@code DELETE  /reviews/:id} : delete the "id" review.
     *
     * @param id the id of the review to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Review : {}", id);

        // fetch review first to know which book it belongs to
        Review review = reviewRepository.findById(id).orElse(null);
        Long bookId = review != null && review.getBook() != null ? review.getBook().getId() : null;

        reviewRepository.deleteById(id);
        reviewSearchRepository.deleteFromIndexById(id);

        // recalc average rating if bookId exists
        if (bookId != null) {
            reviewService.recalculateBookAverageRating(bookId);
        }

        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /reviews/_search?query=:query} : search for the review corresponding to the query.
     *
     * @param query the query of the review search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Review> searchReviews(@RequestParam("query") String query) {
        LOG.debug("REST request to search Reviews for query {}", query);
        try {
            return StreamSupport.stream(reviewSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
