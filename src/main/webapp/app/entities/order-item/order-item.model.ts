import { IBook } from 'app/entities/book/book.model';
import { IOrder } from 'app/entities/order/order.model';

export interface IOrderItem {
  id: number;
  quantity?: number | null;
  price?: number | null;
  book?: IBook | null;
  order?: IOrder | null;
}

export type NewOrderItem = Omit<IOrderItem, 'id'> & { id: null };
