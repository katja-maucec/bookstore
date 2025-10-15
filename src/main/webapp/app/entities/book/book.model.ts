import { ICategory } from 'app/entities/category/category.model';
import { IReview } from 'app/entities/review/review.model';

export interface IBook {
  id: number;
  title?: string | null;
  author?: string | null;
  description?: string | null;
  price?: number | null;
  available?: boolean | null;
  category?: ICategory | null;
  reviews?: IReview[];
  averageRating?: number;
}

export type NewBook = Omit<IBook, 'id'> & { id: null };
