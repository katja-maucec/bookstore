import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IBook } from 'app/entities/book/book.model';
import { BookService } from 'app/entities/book/service/book.service';
import { IShoppingCart } from 'app/entities/shopping-cart/shopping-cart.model';
import { ShoppingCartService } from 'app/entities/shopping-cart/service/shopping-cart.service';
import { ICartItem } from '../cart-item.model';
import { CartItemService } from '../service/cart-item.service';
import { CartItemFormService } from './cart-item-form.service';

import { CartItemUpdateComponent } from './cart-item-update.component';

describe('CartItem Management Update Component', () => {
  let comp: CartItemUpdateComponent;
  let fixture: ComponentFixture<CartItemUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let cartItemFormService: CartItemFormService;
  let cartItemService: CartItemService;
  let bookService: BookService;
  let shoppingCartService: ShoppingCartService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CartItemUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(CartItemUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(CartItemUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    cartItemFormService = TestBed.inject(CartItemFormService);
    cartItemService = TestBed.inject(CartItemService);
    bookService = TestBed.inject(BookService);
    shoppingCartService = TestBed.inject(ShoppingCartService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Book query and add missing value', () => {
      const cartItem: ICartItem = { id: 7701 };
      const book: IBook = { id: 32624 };
      cartItem.book = book;

      const bookCollection: IBook[] = [{ id: 32624 }];
      jest.spyOn(bookService, 'query').mockReturnValue(of(new HttpResponse({ body: bookCollection })));
      const additionalBooks = [book];
      const expectedCollection: IBook[] = [...additionalBooks, ...bookCollection];
      jest.spyOn(bookService, 'addBookToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ cartItem });
      comp.ngOnInit();

      expect(bookService.query).toHaveBeenCalled();
      expect(bookService.addBookToCollectionIfMissing).toHaveBeenCalledWith(
        bookCollection,
        ...additionalBooks.map(expect.objectContaining),
      );
      expect(comp.booksSharedCollection).toEqual(expectedCollection);
    });

    it('should call ShoppingCart query and add missing value', () => {
      const cartItem: ICartItem = { id: 7701 };
      const cart: IShoppingCart = { id: 21091 };
      cartItem.cart = cart;

      const shoppingCartCollection: IShoppingCart[] = [{ id: 21091 }];
      jest.spyOn(shoppingCartService, 'query').mockReturnValue(of(new HttpResponse({ body: shoppingCartCollection })));
      const additionalShoppingCarts = [cart];
      const expectedCollection: IShoppingCart[] = [...additionalShoppingCarts, ...shoppingCartCollection];
      jest.spyOn(shoppingCartService, 'addShoppingCartToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ cartItem });
      comp.ngOnInit();

      expect(shoppingCartService.query).toHaveBeenCalled();
      expect(shoppingCartService.addShoppingCartToCollectionIfMissing).toHaveBeenCalledWith(
        shoppingCartCollection,
        ...additionalShoppingCarts.map(expect.objectContaining),
      );
      expect(comp.shoppingCartsSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const cartItem: ICartItem = { id: 7701 };
      const book: IBook = { id: 32624 };
      cartItem.book = book;
      const cart: IShoppingCart = { id: 21091 };
      cartItem.cart = cart;

      activatedRoute.data = of({ cartItem });
      comp.ngOnInit();

      expect(comp.booksSharedCollection).toContainEqual(book);
      expect(comp.shoppingCartsSharedCollection).toContainEqual(cart);
      expect(comp.cartItem).toEqual(cartItem);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICartItem>>();
      const cartItem = { id: 2227 };
      jest.spyOn(cartItemFormService, 'getCartItem').mockReturnValue(cartItem);
      jest.spyOn(cartItemService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ cartItem });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: cartItem }));
      saveSubject.complete();

      // THEN
      expect(cartItemFormService.getCartItem).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(cartItemService.update).toHaveBeenCalledWith(expect.objectContaining(cartItem));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICartItem>>();
      const cartItem = { id: 2227 };
      jest.spyOn(cartItemFormService, 'getCartItem').mockReturnValue({ id: null });
      jest.spyOn(cartItemService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ cartItem: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: cartItem }));
      saveSubject.complete();

      // THEN
      expect(cartItemFormService.getCartItem).toHaveBeenCalled();
      expect(cartItemService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICartItem>>();
      const cartItem = { id: 2227 };
      jest.spyOn(cartItemService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ cartItem });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(cartItemService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareBook', () => {
      it('should forward to bookService', () => {
        const entity = { id: 32624 };
        const entity2 = { id: 17120 };
        jest.spyOn(bookService, 'compareBook');
        comp.compareBook(entity, entity2);
        expect(bookService.compareBook).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareShoppingCart', () => {
      it('should forward to shoppingCartService', () => {
        const entity = { id: 21091 };
        const entity2 = { id: 19091 };
        jest.spyOn(shoppingCartService, 'compareShoppingCart');
        comp.compareShoppingCart(entity, entity2);
        expect(shoppingCartService.compareShoppingCart).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
