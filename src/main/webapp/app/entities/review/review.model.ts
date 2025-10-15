import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { IBook } from 'app/entities/book/book.model';

export interface IReview {
  id: number;
  rating?: number | null;
  comment?: string | null;
  createdAt?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
  book?: IBook | null;
}

export type NewReview = Omit<IReview, 'id'> & { id: null };

/**
 * Class that works for both new and existing reviews
 */
export class Review {
  constructor(
    public id: number | null = null,
    public rating?: number | null,
    public comment?: string | null,
    public createdAt?: dayjs.Dayjs | null,
    public user?: Pick<IUser, 'id' | 'login'> | null,
    public book?: IBook | null,
  ) {}

  /**
   * Helper method to easily convert this object into a NewReview for creation
   */
  toNewReview(): NewReview {
    return {
      ...this,
      id: null,
    };
  }
}
