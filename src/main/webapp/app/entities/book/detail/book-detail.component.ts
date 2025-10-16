import { Component, inject, input, signal, effect, WritableSignal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IBook } from '../book.model';
import { IReview } from '../../review/review.model';
import { EntityArrayResponseType, ReviewService } from '../../review/service/review.service';
import { Authority } from '../../../config/authority.constants';
import { AccountService } from '../../../core/auth/account.service';
import { faStar as farStar } from '@fortawesome/free-regular-svg-icons';
import { faStar } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import dayjs from 'dayjs/esm';
import { ReviewDeleteDialogComponent } from '../../review/delete/review-delete-dialog.component';
import { filter, Observable, tap } from 'rxjs';
import { ITEM_DELETED_EVENT } from '../../../config/navigation.constants';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { SortService, sortStateSignal } from '../../../shared/sort';
import { BookService } from '../service/book.service';
import { RefreshService } from '../../../shared/refresh.service';

@Component({
  selector: 'jhi-book-detail',
  templateUrl: './book-detail.component.html',
  styleUrls: ['./book-detail.component.scss'],
  imports: [SharedModule, RouterModule, FontAwesomeModule],
})
export class BookDetailComponent {
  book = input<IBook | null>(null);
  reviews = signal<IReview[]>([]);
  isAdmin = signal(false);
  isLoading = false;
  sortState = sortStateSignal({});
  currentSearch = '';
  displayBook = signal<IBook | null>(null);

  faStar = faStar;
  farStar = farStar;

  protected accountService = inject(AccountService);
  protected reviewService = inject(ReviewService);
  protected router = inject(Router);
  protected modalService = inject(NgbModal);
  protected readonly sortService = inject(SortService);
  protected bookService = inject(BookService);
  protected refreshService = inject(RefreshService);

  constructor() {
    // React to book changes
    effect(() => {
      const bookRef = this.book();
      if (bookRef?.id) {
        this.displayBook.set({ ...bookRef });
        this.loadReviews(bookRef.id);
      } else {
        this.displayBook.set(null);
        this.reviews.set([]);
      }
    });
  }

  ngOnInit(): void {
    // Set admin flag
    this.accountService.identity().subscribe(account => {
      this.isAdmin.set(account?.authorities.includes(Authority.ADMIN) ?? false);
    });

    // Listen for refresh events to reload book data
    this.refreshService.onRefresh().subscribe(() => {
      const bookId = this.book()?.id;
      if (bookId) {
        this.reloadBook(bookId);
      }
    });
  }

  private reloadBook(bookId: number): void {
    this.bookService.find(bookId).subscribe({
      next: res => {
        if (res.body) {
          this.displayBook.set(res.body);
        }
      },
      error: err => console.error('Error reloading book', err),
    });
  }

  private loadReviews(bookId: number): void {
    this.reviewService.getByBook(bookId).subscribe({
      next: res => {
        const reviewsWithDates = (res ?? []).map(r => ({
          ...r,
          createdAt: r.createdAt ? dayjs(r.createdAt) : null,
        }));
        this.reviews.set(reviewsWithDates);
      },
      error: err => console.error('Error loading reviews', err),
    });
  }

  delete(review: IReview): void {
    const modalRef = this.modalService.open(ReviewDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.review = review;

    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => {
          const bookId = this.book()?.id;
          if (bookId) {
            // reload reviews
            this.loadReviews(bookId);

            // fetch updated book data
            this.bookService.find(bookId).subscribe(res => {
              if (res.body) {
                this.displayBook.set(res.body);
              }
            });
          }
        }),
      )
      .subscribe();
  }

  previousState(): void {
    window.history.back();
  }
}
