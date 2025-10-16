package com.stoecklin.bookstore.web.rest;

import com.stoecklin.bookstore.domain.CartItem;
import com.stoecklin.bookstore.domain.ShoppingCart;
import com.stoecklin.bookstore.repository.ShoppingCartRepository;
import com.stoecklin.bookstore.repository.UserRepository;
import com.stoecklin.bookstore.repository.search.ShoppingCartSearchRepository;
import com.stoecklin.bookstore.security.SecurityUtils;
import com.stoecklin.bookstore.service.ShoppingCartService;
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
 * REST controller for managing {@link com.stoecklin.bookstore.domain.ShoppingCart}.
 */
@RestController
@RequestMapping("/api/shopping-carts")
@Transactional
public class ShoppingCartResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartResource.class);

    private static final String ENTITY_NAME = "shoppingCart";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShoppingCartRepository shoppingCartRepository;

    private final ShoppingCartSearchRepository shoppingCartSearchRepository;

    private final ShoppingCartService shoppingCartService;

    private final UserRepository userRepository;

    public ShoppingCartResource(
        ShoppingCartRepository shoppingCartRepository,
        ShoppingCartSearchRepository shoppingCartSearchRepository,
        ShoppingCartService shoppingCartService,
        UserRepository userRepository
    ) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.shoppingCartSearchRepository = shoppingCartSearchRepository;
        this.shoppingCartService = shoppingCartService;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST  /shopping-carts} : Create a new shoppingCart.
     *
     * @param shoppingCart the shoppingCart to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shoppingCart, or with
     *     status {@code 400 (Bad Request)} if the shoppingCart has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ShoppingCart> createShoppingCart(@Valid @RequestBody ShoppingCart shoppingCart) throws URISyntaxException {
        LOG.debug("REST request to save ShoppingCart : {}", shoppingCart);
        if (shoppingCart.getId() != null) {
            throw new BadRequestAlertException("A new shoppingCart cannot already have an ID", ENTITY_NAME, "idexists");
        }
        shoppingCart = shoppingCartRepository.save(shoppingCart);
        shoppingCartSearchRepository.index(shoppingCart);
        return ResponseEntity.created(new URI("/api/shopping-carts/" + shoppingCart.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, shoppingCart.getId().toString()))
            .body(shoppingCart);
    }

    /**
     * {@code POST  /shopping-carts/add-book} : Add a book to the current user's cart.
     *
     * @param bookId the ID of the book to add
     * @param quantity the quantity to add (default 1)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the cart item
     */
    @PostMapping("/add-book")
    public ResponseEntity<CartItem> addBookToCart(@RequestParam Long bookId, @RequestParam(defaultValue = "1") Integer quantity) {
        LOG.debug("REST request to add book {} to cart", bookId);
        CartItem cartItem = shoppingCartService.addItem(shoppingCartService.getOrCreateCurrentUserCart().getId(), bookId, quantity);
        return ResponseEntity.ok().body(cartItem);
    }

    /**
     * {@code GET  /shopping-carts/my-cart} : Get the current user's active cart.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the shopping cart
     */
    @GetMapping("/my-cart")
    public ResponseEntity<ShoppingCart> getMyCart() {
        LOG.debug("REST request to get current user's cart");
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("User not logged in"));

        Optional<ShoppingCart> cart = userRepository
            .findOneByLogin(login)
            .flatMap(user -> shoppingCartRepository.findByUserAndCompletedWithItems(user.getId(), false));

        return ResponseUtil.wrapOrNotFound(cart);
    }

    /**
     * {@code SEARCH  /shopping-carts/_search?query=:query} : search for the shoppingCart corresponding to the query.
     *
     * /** {@code PUT  /shopping-carts/:id} : Updates an existing shoppingCart.
     *
     * @param id the id of the shoppingCart to save.
     * @param shoppingCart the shoppingCart to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shoppingCart, or with
     *     status {@code 400 (Bad Request)} if the shoppingCart is not valid, or with status
     *     {@code 500 (Internal Server Error)} if the shoppingCart couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingCart> updateShoppingCart(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ShoppingCart shoppingCart
    ) throws URISyntaxException {
        LOG.debug("REST request to update ShoppingCart : {}, {}", id, shoppingCart);
        if (shoppingCart.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shoppingCart.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shoppingCartRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        shoppingCart = shoppingCartRepository.save(shoppingCart);
        shoppingCartSearchRepository.index(shoppingCart);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, shoppingCart.getId().toString()))
            .body(shoppingCart);
    }

    /**
     * {@code PATCH  /shopping-carts/:id} : Partial updates given fields of an existing shoppingCart, field will ignore
     * if it is null
     *
     * @param id the id of the shoppingCart to save.
     * @param shoppingCart the shoppingCart to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shoppingCart, or with
     *     status {@code 400 (Bad Request)} if the shoppingCart is not valid, or with status {@code 404 (Not Found)} if
     *     the shoppingCart is not found, or with status {@code 500 (Internal Server Error)} if the shoppingCart
     *     couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ShoppingCart> partialUpdateShoppingCart(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ShoppingCart shoppingCart
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ShoppingCart partially : {}, {}", id, shoppingCart);
        if (shoppingCart.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shoppingCart.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shoppingCartRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ShoppingCart> result = shoppingCartRepository
            .findById(shoppingCart.getId())
            .map(existingShoppingCart -> {
                if (shoppingCart.getCreatedAt() != null) {
                    existingShoppingCart.setCreatedAt(shoppingCart.getCreatedAt());
                }
                if (shoppingCart.getCompleted() != null) {
                    existingShoppingCart.setCompleted(shoppingCart.getCompleted());
                }

                return existingShoppingCart;
            })
            .map(shoppingCartRepository::save)
            .map(savedShoppingCart -> {
                shoppingCartSearchRepository.index(savedShoppingCart);
                return savedShoppingCart;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, shoppingCart.getId().toString())
        );
    }

    /**
     * {@code GET  /shopping-carts} : get all the shoppingCarts.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shoppingCarts in body.
     */
    @GetMapping("")
    public List<ShoppingCart> getAllShoppingCarts(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all ShoppingCarts");
        if (eagerload) {
            return shoppingCartRepository.findAllWithEagerRelationships();
        } else {
            return shoppingCartRepository.findAll();
        }
    }

    /**
     * {@code GET  /shopping-carts/:id} : get the "id" shoppingCart.
     *
     * @param id the id of the shoppingCart to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shoppingCart, or with status
     *     {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingCart> getShoppingCart(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ShoppingCart : {}", id);
        Optional<ShoppingCart> shoppingCart = shoppingCartRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(shoppingCart);
    }

    /**
     * {@code DELETE  /shopping-carts/:id} : delete the "id" shoppingCart.
     *
     * @param id the id of the shoppingCart to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShoppingCart(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ShoppingCart : {}", id);
        shoppingCartRepository.deleteById(id);
        shoppingCartSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /shopping-carts/_search?query=:query} : search for the shoppingCart corresponding to the query.
     *
     * @param query the query of the shoppingCart search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<ShoppingCart> searchShoppingCarts(@RequestParam("query") String query) {
        LOG.debug("REST request to search ShoppingCarts for query {}", query);
        try {
            return StreamSupport.stream(shoppingCartSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
