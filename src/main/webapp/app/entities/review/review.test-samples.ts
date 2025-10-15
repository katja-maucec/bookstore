import dayjs from 'dayjs/esm';

import { IReview, NewReview } from './review.model';

export const sampleWithRequiredData: IReview = {
  id: 6640,
  rating: 2,
};

export const sampleWithPartialData: IReview = {
  id: 12934,
  rating: 2,
  createdAt: dayjs('2025-09-30T12:42'),
};

export const sampleWithFullData: IReview = {
  id: 30916,
  rating: 4,
  comment: 'yowza',
  createdAt: dayjs('2025-09-30T17:06'),
};

export const sampleWithNewData: NewReview = {
  rating: 2,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
