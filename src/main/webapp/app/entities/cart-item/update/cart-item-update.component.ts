import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { IShoppingCart } from 'app/entities/shopping-cart/shopping-cart.model';
import { ShoppingCartService } from 'app/entities/shopping-cart/service/shopping-cart.service';
import { CartItemService } from '../service/cart-item.service';
import { ICartItem } from '../cart-item.model';
import { CartItemFormGroup, CartItemFormService } from './cart-item-form.service';

@Component({
  selector: 'jhi-cart-item-update',
  templateUrl: './cart-item-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class CartItemUpdateComponent implements OnInit {
  isSaving = false;
  cartItem: ICartItem | null = null;

  booksSharedCollection: IBook[] = [];
  shoppingCartsSharedCollection: IShoppingCart[] = [];

  protected cartItemService = inject(CartItemService);
  protected cartItemFormService = inject(CartItemFormService);
  protected bookService = inject(BookService);
  protected shoppingCartService = inject(ShoppingCartService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CartItemFormGroup = this.cartItemFormService.createCartItemFormGroup();

  compareBook = (o1: IBook | null, o2: IBook | null): boolean => this.bookService.compareBook(o1, o2);

  compareShoppingCart = (o1: IShoppingCart | null, o2: IShoppingCart | null): boolean =>
    this.shoppingCartService.compareShoppingCart(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ cartItem }) => {
      this.cartItem = cartItem;
      if (cartItem) {
        this.updateForm(cartItem);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const cartItem = this.cartItemFormService.getCartItem(this.editForm);
    if (cartItem.id !== null) {
      this.subscribeToSaveResponse(this.cartItemService.update(cartItem));
    } else {
      this.subscribeToSaveResponse(this.cartItemService.create(cartItem));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICartItem>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(cartItem: ICartItem): void {
    this.cartItem = cartItem;
    this.cartItemFormService.resetForm(this.editForm, cartItem);

    this.booksSharedCollection = this.bookService.addBookToCollectionIfMissing<IBook>(this.booksSharedCollection, cartItem.book);
    this.shoppingCartsSharedCollection = this.shoppingCartService.addShoppingCartToCollectionIfMissing<IShoppingCart>(
      this.shoppingCartsSharedCollection,
      cartItem.cart,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.bookService
      .query()
      .pipe(map((res: HttpResponse<IBook[]>) => res.body ?? []))
      .pipe(map((books: IBook[]) => this.bookService.addBookToCollectionIfMissing<IBook>(books, this.cartItem?.book)))
      .subscribe((books: IBook[]) => (this.booksSharedCollection = books));

    this.shoppingCartService
      .query()
      .pipe(map((res: HttpResponse<IShoppingCart[]>) => res.body ?? []))
      .pipe(
        map((shoppingCarts: IShoppingCart[]) =>
          this.shoppingCartService.addShoppingCartToCollectionIfMissing<IShoppingCart>(shoppingCarts, this.cartItem?.cart),
        ),
      )
      .subscribe((shoppingCarts: IShoppingCart[]) => (this.shoppingCartsSharedCollection = shoppingCarts));
  }
}
