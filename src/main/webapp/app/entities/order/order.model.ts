import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { OrderStatus } from 'app/entities/enumerations/order-status.model';
import { IOrderItem } from '../order-item/order-item.model';

export interface IOrder {
  id: number;
  totalPrice?: number | null;
  placedAt?: dayjs.Dayjs | null;
  status?: keyof typeof OrderStatus | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  items?: IOrderItem[] | null;
}

export type NewOrder = Omit<IOrder, 'id'> & { id: null };
