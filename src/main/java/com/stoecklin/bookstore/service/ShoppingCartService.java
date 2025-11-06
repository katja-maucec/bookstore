package com.stoecklin.bookstore.service;

import com.stoecklin.bookstore.domain.Book;
import com.stoecklin.bookstore.domain.CartItem;
import com.stoecklin.bookstore.domain.Order;
import com.stoecklin.bookstore.domain.OrderItem;
import com.stoecklin.bookstore.domain.ShoppingCart;
import com.stoecklin.bookstore.domain.User;
import com.stoecklin.bookstore.domain.enumeration.OrderStatus;
import com.stoecklin.bookstore.repository.BookRepository;
import com.stoecklin.bookstore.repository.CartItemRepository;
import com.stoecklin.bookstore.repository.OrderItemRepository;
import com.stoecklin.bookstore.repository.OrderRepository;
import com.stoecklin.bookstore.repository.ShoppingCartRepository;
import com.stoecklin.bookstore.repository.UserRepository;
import com.stoecklin.bookstore.security.SecurityUtils;
import java.time.Instant;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public ShoppingCartService(
        ShoppingCartRepository cartRepository,
        CartItemRepository cartItemRepository,
        BookRepository bookRepository,
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    public ShoppingCart getOrCreateCurrentUserCart() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("User not logged in"));

        User user = userRepository.findOneByLogin(login).orElseThrow(() -> new IllegalStateException("User not found: " + login));

        return cartRepository
            .findOneWithEagerRelationshipsByUserAndCompletedFalse(user)
            .orElseGet(() -> {
                ShoppingCart c = new ShoppingCart();
                c.setCreatedAt(Instant.now());
                c.setCompleted(false);
                c.setUser(user);
                c.setItems(new java.util.HashSet<>());
                return cartRepository.save(c);
            });
    }

    public CartItem addItem(Long bookId, Integer quantity) {
        ShoppingCart cart = getOrCreateCurrentUserCart();

        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalStateException("Book not found"));

        CartItem item = cartItemRepository
            .findByCartAndBook(cart, book)
            .map(existing -> {
                existing.setQuantity(existing.getQuantity() + quantity);
                return existing;
            })
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setBook(book);
                newItem.setQuantity(quantity);
                return newItem;
            });

        return cartItemRepository.save(item);
    }

    public void removeItem(Long cartId, Long itemId) {
        cartItemRepository.findById(itemId).ifPresent(cartItemRepository::delete);
    }

    public void checkout(Long cartId) {
        ShoppingCart cart = cartRepository.findById(cartId).orElseThrow();
        // create order
        Order order = new Order();
        order.setPlacedAt(Instant.now());
        order.setUser(cart.getUser());
        // compute total
        java.math.BigDecimal total = cart
            .getItems()
            .stream()
            .map(ci -> ci.getBook().getPrice().multiply(java.math.BigDecimal.valueOf(ci.getQuantity())))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // create order items
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setBook(ci.getBook());
            oi.setPrice(ci.getBook().getPrice());
            oi.setQuantity(ci.getQuantity());
            orderItemRepository.save(oi);
        }
        // mark cart completed/archived (add a boolean field 'completed' to ShoppingCart if you like)
        cart.setCompleted(true);
        cartRepository.save(cart);

        // Automatically create a new empty cart
        ShoppingCart newCart = new ShoppingCart();
        newCart.setUser(cart.getUser());
        newCart.setCreatedAt(Instant.now());
        newCart.setCompleted(false);
        newCart.setItems(new HashSet<>());
        cartRepository.save(newCart);
    }
}
