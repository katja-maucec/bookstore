import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import SharedModule from 'app/shared/shared.module';
import { IShoppingCart } from '../shopping-cart.model';
import { ShoppingCartService } from '../service/shopping-cart.service';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AccountService } from 'app/core/auth/account.service';
import { Authority } from 'app/config/authority.constants';

@Component({
  selector: 'jhi-shopping-cart',
  templateUrl: './shopping-cart.component.html',
  standalone: true,
  imports: [SharedModule, RouterModule, FontAwesomeModule],
})
export class ShoppingCartComponent implements OnInit {
  cart = signal<IShoppingCart | null>(null);
  isLoading = false;
  isAdmin = signal(false);

  private readonly shoppingCartService = inject(ShoppingCartService);
  private readonly router = inject(Router);
  private readonly accountService = inject(AccountService);

  ngOnInit(): void {
    // Check if user is admin
    this.accountService.identity().subscribe(account => {
      const admin = account?.authorities.includes(Authority.ADMIN) ?? false;
      this.isAdmin.set(admin);

      // If not admin, load cart
      if (!admin) {
        this.loadCart();
      }
    });
  }

  loadCart(): void {
    this.isLoading = true;
    this.shoppingCartService.getMyCart().subscribe({
      next: res => {
        this.cart.set(res.body);
        this.isLoading = false;
      },
      error: err => {
        console.error('Error loading cart:', err);
        this.isLoading = false;
      },
    });
  }

  getTotalPrice(): number {
    const cartValue = this.cart();
    if (!cartValue?.items) {
      return 0;
    }
    return cartValue.items.reduce((total, item) => {
      const price = item.book?.price ?? 0;
      const quantity = item.quantity ?? 0;
      return total + price * quantity;
    }, 0);
  }

  continueShopping(): void {
    this.router.navigate(['/book']);
  }

  placeOrder(): void {
    if (!this.cart()?.items || this.cart()!.items!.length === 0) {
      return;
    }

    this.isLoading = true;
    this.shoppingCartService.placeOrder().subscribe({
      next: () => {
        this.isLoading = false;
        // Navigate to orders page
        this.router.navigate(['/order']);
      },
      error: err => {
        console.error('Error placing order:', err);
        this.isLoading = false;
        // You might want to show an error message here
        alert('Failed to place order. Please try again.');
      },
    });
  }
}
