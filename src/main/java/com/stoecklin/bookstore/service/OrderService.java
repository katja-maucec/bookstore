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
import com.stoecklin.bookstore.repository.OrderRepository;
import com.stoecklin.bookstore.repository.ShoppingCartRepository;
import com.stoecklin.bookstore.repository.UserRepository;
import com.stoecklin.bookstore.repository.search.OrderSearchRepository;
import com.stoecklin.bookstore.security.SecurityUtils;
import com.stoecklin.bookstore.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderSearchRepository orderSearchRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(
        OrderRepository orderRepository,
        OrderSearchRepository orderSearchRepository,
        ShoppingCartRepository shoppingCartRepository,
        BookRepository bookRepository,
        UserRepository userRepository,
        CartItemRepository cartItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderSearchRepository = orderSearchRepository;
        this.shoppingCartRepository = shoppingCartRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Place an order from the current user's shopping cart
     */
    public Order placeOrderFromCart() {
        LOG.debug("Request to place order from shopping cart");

        // Get current user's cart
        String userLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new BadRequestAlertException("User not authenticated", "Order", "notauthenticated"));

        User user = userRepository
            .findOneByLogin(userLogin)
            .orElseThrow(() -> new BadRequestAlertException("User not found", "User", "usernotfound"));

        ShoppingCart cart = shoppingCartRepository
            .findByUserAndCompletedWithItems(user.getId(), false)
            .orElseThrow(() -> new BadRequestAlertException("Shopping cart not found", "ShoppingCart", "notfound"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestAlertException("Shopping cart is empty", "ShoppingCart", "cartempty");
        }

        // Create new order
        Order order = new Order();
        order.setPlacedAt(Instant.now());
        order.setStatus(OrderStatus.PENDING);
        order.setUser(user);

        BigDecimal totalPrice = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();

        // Keep track of cart items to delete
        Set<CartItem> cartItemsToDelete = new HashSet<>(cart.getItems());

        // Convert cart items to order items and update stock
        for (CartItem cartItem : cartItemsToDelete) {
            Book book = cartItem.getBook();

            // Check stock availability
            if (book.getStock() < cartItem.getQuantity()) {
                throw new BadRequestAlertException("Insufficient stock for book: " + book.getTitle(), "Book", "insufficientstock");
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(book.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            // Calculate total
            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            // Decrement stock
            book.setStock(book.getStock() - cartItem.getQuantity());
            bookRepository.save(book);
        }

        order.setTotalPrice(totalPrice);
        order.setItems(orderItems);

        // Save order
        order = orderRepository.save(order);
        orderSearchRepository.index(order);

        // Delete cart items explicitly
        for (CartItem cartItem : cartItemsToDelete) {
            cart.removeItems(cartItem);
            cartItemRepository.delete(cartItem);
        }

        // Save the cart after clearing items
        shoppingCartRepository.save(cart);

        LOG.debug("Order placed successfully with ID: {}", order.getId());
        return order;
    }
}
