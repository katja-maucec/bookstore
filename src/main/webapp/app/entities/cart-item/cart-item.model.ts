import { IBook } from 'app/entities/book/book.model';
import { IShoppingCart } from 'app/entities/shopping-cart/shopping-cart.model';

export interface ICartItem {
  id: number;
  quantity?: number | null;
  book?: IBook | null;
  cart?: IShoppingCart | null;
}

export type NewCartItem = Omit<ICartItem, 'id'> & { id: null };
