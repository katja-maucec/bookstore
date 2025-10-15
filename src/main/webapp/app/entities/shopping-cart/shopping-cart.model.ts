import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IShoppingCart {
  id: number;
  createdAt?: dayjs.Dayjs | null;
  completed?: boolean | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewShoppingCart = Omit<IShoppingCart, 'id'> & { id: null };
