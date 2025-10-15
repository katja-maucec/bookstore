import dayjs from 'dayjs/esm';

import { IOrder, NewOrder } from './order.model';

export const sampleWithRequiredData: IOrder = {
  id: 26110,
  totalPrice: 21449.77,
  placedAt: dayjs('2025-09-30T18:19'),
  status: 'PENDING',
};

export const sampleWithPartialData: IOrder = {
  id: 19840,
  totalPrice: 20719.21,
  placedAt: dayjs('2025-09-30T16:44'),
  status: 'PAID',
};

export const sampleWithFullData: IOrder = {
  id: 27813,
  totalPrice: 14949.25,
  placedAt: dayjs('2025-09-30T17:47'),
  status: 'PENDING',
};

export const sampleWithNewData: NewOrder = {
  totalPrice: 12844.42,
  placedAt: dayjs('2025-10-01T03:46'),
  status: 'PENDING',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
