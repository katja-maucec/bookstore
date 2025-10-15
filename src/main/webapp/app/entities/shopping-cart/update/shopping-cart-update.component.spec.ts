import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { ShoppingCartService } from '../service/shopping-cart.service';
import { IShoppingCart } from '../shopping-cart.model';
import { ShoppingCartFormService } from './shopping-cart-form.service';

import { ShoppingCartUpdateComponent } from './shopping-cart-update.component';

describe('ShoppingCart Management Update Component', () => {
  let comp: ShoppingCartUpdateComponent;
  let fixture: ComponentFixture<ShoppingCartUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let shoppingCartFormService: ShoppingCartFormService;
  let shoppingCartService: ShoppingCartService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ShoppingCartUpdateComponent],
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
      .overrideTemplate(ShoppingCartUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ShoppingCartUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    shoppingCartFormService = TestBed.inject(ShoppingCartFormService);
    shoppingCartService = TestBed.inject(ShoppingCartService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const shoppingCart: IShoppingCart = { id: 19091 };
      const user: IUser = { id: 3944 };
      shoppingCart.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ shoppingCart });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const shoppingCart: IShoppingCart = { id: 19091 };
      const user: IUser = { id: 3944 };
      shoppingCart.user = user;

      activatedRoute.data = of({ shoppingCart });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContainEqual(user);
      expect(comp.shoppingCart).toEqual(shoppingCart);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IShoppingCart>>();
      const shoppingCart = { id: 21091 };
      jest.spyOn(shoppingCartFormService, 'getShoppingCart').mockReturnValue(shoppingCart);
      jest.spyOn(shoppingCartService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ shoppingCart });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: shoppingCart }));
      saveSubject.complete();

      // THEN
      expect(shoppingCartFormService.getShoppingCart).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(shoppingCartService.update).toHaveBeenCalledWith(expect.objectContaining(shoppingCart));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IShoppingCart>>();
      const shoppingCart = { id: 21091 };
      jest.spyOn(shoppingCartFormService, 'getShoppingCart').mockReturnValue({ id: null });
      jest.spyOn(shoppingCartService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ shoppingCart: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: shoppingCart }));
      saveSubject.complete();

      // THEN
      expect(shoppingCartFormService.getShoppingCart).toHaveBeenCalled();
      expect(shoppingCartService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IShoppingCart>>();
      const shoppingCart = { id: 21091 };
      jest.spyOn(shoppingCartService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ shoppingCart });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(shoppingCartService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
