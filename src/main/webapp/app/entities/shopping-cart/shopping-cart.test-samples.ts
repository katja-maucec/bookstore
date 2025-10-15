import dayjs from 'dayjs/esm';

import { IShoppingCart, NewShoppingCart } from './shopping-cart.model';

export const sampleWithRequiredData: IShoppingCart = {
  id: 26783,
  createdAt: dayjs('2025-10-01T05:57'),
  completed: false,
};

export const sampleWithPartialData: IShoppingCart = {
  id: 26454,
  createdAt: dayjs('2025-10-01T10:21'),
  completed: true,
};

export const sampleWithFullData: IShoppingCart = {
  id: 18836,
  createdAt: dayjs('2025-10-01T00:49'),
  completed: true,
};

export const sampleWithNewData: NewShoppingCart = {
  createdAt: dayjs('2025-09-30T17:04'),
  completed: false,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
