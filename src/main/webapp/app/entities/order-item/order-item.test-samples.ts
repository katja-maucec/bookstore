import { IOrderItem, NewOrderItem } from './order-item.model';

export const sampleWithRequiredData: IOrderItem = {
  id: 16549,
  quantity: 23987,
  price: 22506.51,
};

export const sampleWithPartialData: IOrderItem = {
  id: 28487,
  quantity: 27299,
  price: 22462.83,
};

export const sampleWithFullData: IOrderItem = {
  id: 6728,
  quantity: 17449,
  price: 20263.67,
};

export const sampleWithNewData: NewOrderItem = {
  quantity: 12164,
  price: 1027.11,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
