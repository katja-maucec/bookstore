import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { ICartItem, NewCartItem } from '../cart-item.model';

export type PartialUpdateCartItem = Partial<ICartItem> & Pick<ICartItem, 'id'>;

export type EntityResponseType = HttpResponse<ICartItem>;
export type EntityArrayResponseType = HttpResponse<ICartItem[]>;

@Injectable({ providedIn: 'root' })
export class CartItemService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/cart-items');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/cart-items/_search');

  create(cartItem: NewCartItem): Observable<EntityResponseType> {
    return this.http.post<ICartItem>(this.resourceUrl, cartItem, { observe: 'response' });
  }

  update(cartItem: ICartItem): Observable<EntityResponseType> {
    return this.http.put<ICartItem>(`${this.resourceUrl}/${this.getCartItemIdentifier(cartItem)}`, cartItem, { observe: 'response' });
  }

  partialUpdate(cartItem: PartialUpdateCartItem): Observable<EntityResponseType> {
    return this.http.patch<ICartItem>(`${this.resourceUrl}/${this.getCartItemIdentifier(cartItem)}`, cartItem, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICartItem>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICartItem[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICartItem[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<ICartItem[]>()], asapScheduler)));
  }

  getCartItemIdentifier(cartItem: Pick<ICartItem, 'id'>): number {
    return cartItem.id;
  }

  compareCartItem(o1: Pick<ICartItem, 'id'> | null, o2: Pick<ICartItem, 'id'> | null): boolean {
    return o1 && o2 ? this.getCartItemIdentifier(o1) === this.getCartItemIdentifier(o2) : o1 === o2;
  }

  addCartItemToCollectionIfMissing<Type extends Pick<ICartItem, 'id'>>(
    cartItemCollection: Type[],
    ...cartItemsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const cartItems: Type[] = cartItemsToCheck.filter(isPresent);
    if (cartItems.length > 0) {
      const cartItemCollectionIdentifiers = cartItemCollection.map(cartItemItem => this.getCartItemIdentifier(cartItemItem));
      const cartItemsToAdd = cartItems.filter(cartItemItem => {
        const cartItemIdentifier = this.getCartItemIdentifier(cartItemItem);
        if (cartItemCollectionIdentifiers.includes(cartItemIdentifier)) {
          return false;
        }
        cartItemCollectionIdentifiers.push(cartItemIdentifier);
        return true;
      });
      return [...cartItemsToAdd, ...cartItemCollection];
    }
    return cartItemCollection;
  }
}
