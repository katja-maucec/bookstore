import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 3991,
  title: 'blue regal',
  author: 'nocturnal wordy',
  price: 18654.5,
  available: false,
};

export const sampleWithPartialData: IBook = {
  id: 20784,
  title: 'blah legislature',
  author: 'upwardly',
  price: 23765.99,
  available: true,
};

export const sampleWithFullData: IBook = {
  id: 8637,
  title: 'sniff',
  author: 'however quicker',
  description: 'reasoning repeatedly instead',
  price: 23702.65,
  available: true,
};

export const sampleWithNewData: NewBook = {
  title: 'taxicab nor',
  author: 'agreeable boo',
  price: 31159.82,
  available: true,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
