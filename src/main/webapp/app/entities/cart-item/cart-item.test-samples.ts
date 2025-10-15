import { ICartItem, NewCartItem } from './cart-item.model';

export const sampleWithRequiredData: ICartItem = {
  id: 12545,
  quantity: 12390,
};

export const sampleWithPartialData: ICartItem = {
  id: 2454,
  quantity: 14800,
};

export const sampleWithFullData: ICartItem = {
  id: 9768,
  quantity: 32077,
};

export const sampleWithNewData: NewCartItem = {
  quantity: 917,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
