import { Component, NgZone, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faStar as farStar } from '@fortawesome/free-regular-svg-icons';
import { faStar } from '@fortawesome/free-solid-svg-icons';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { IBook } from '../book.model';
import { BookService, EntityArrayResponseType } from '../service/book.service';
import { BookDeleteDialogComponent } from '../delete/book-delete-dialog.component';
import { AccountService } from 'app/core/auth/account.service';
import { Authority } from 'app/config/authority.constants';
import { IReview } from '../../review/review.model';
import { RefreshService } from '../../../shared/refresh.service';
import { ShoppingCartService } from '../../shopping-cart/service/shopping-cart.service';
import { Account } from '../../../core/auth/account.model';

type BookWithAverage = IBook & {
  reviews: IReview[]; // always an array
  averageRating: number; // always a number
};

@Component({
  selector: 'jhi-book',
  templateUrl: './book.component.html',
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, FontAwesomeModule],
})
export class BookComponent implements OnInit {
  private static readonly NOT_SORTABLE_FIELDS_AFTER_SEARCH = ['title', 'author', 'description'];

  subscription: Subscription | null = null;
  books = signal<BookWithAverage[]>([]);

  account = signal<Account | null>(null);

  isLoading = false;

  sortState = sortStateSignal({});
  currentSearch = '';

  isAdmin = signal(false);

  faStar = faStar;
  farStar = farStar;

  public readonly router = inject(Router);
  protected readonly bookService = inject(BookService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);
  protected accountService = inject(AccountService);
  protected refreshService = inject(RefreshService);
  protected shoppingCartService = inject(ShoppingCartService);

  trackId = (book: Pick<IBook, 'id'>): number => this.bookService.getBookIdentifier(book);

  ngOnInit(): void {
    this.accountService.identity().subscribe(account => {
      this.isAdmin.set(account?.authorities.includes(Authority.ADMIN) ?? false);
    });
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
    this.refreshService.onRefresh().subscribe(() => {
      this.load();
    });
  }

  search(query: string): void {
    this.currentSearch = query;
    const { predicate } = this.sortState();
    if (query && predicate && BookComponent.NOT_SORTABLE_FIELDS_AFTER_SEARCH.includes(predicate)) {
      this.navigateToWithComponentValues(this.getDefaultSortState());
      return;
    }
    this.navigateToWithComponentValues(this.sortState());
  }

  getDefaultSortState(): SortState {
    return this.sortService.parseSortParam(this.activatedRoute.snapshot.data[DEFAULT_SORT_DATA]);
  }

  delete(book: IBook): void {
    const modalRef = this.modalService.open(BookDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.book = book;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  averageRating(book: any): number {
    return Number(book.averageRating) || 0;
  }

  getStars(book: any): number[] {
    return [1, 2, 3, 4, 5];
  }

  addToCart(book: IBook): void {
    if (!book.id || (book.stock ?? 0) <= 0) {
      return;
    }

    this.shoppingCartService.addBookToCart(book.id, 1).subscribe({
      next: () => {
        alert(`Added "${book.title}" to your cart!`);
      },
      error: err => {
        console.error('Error adding to cart:', err);
        alert('Failed to add book to cart. Please try again.');
      },
    });
  }

  canAddToCart(book: IBook): boolean {
    return !this.isAdmin() && (book.stock ?? 0) > 0;
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(event, this.currentSearch);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
    if (params.has('search') && params.get('search') !== '') {
      this.currentSearch = params.get('search') as string;
      const { predicate } = this.sortState();
      if (predicate && BookComponent.NOT_SORTABLE_FIELDS_AFTER_SEARCH.includes(predicate)) {
        this.sortState.set({});
      }
    }
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    const booksWithAverage: BookWithAverage[] = dataFromBody.map(book => ({
      ...book,
      reviews: book.reviews ?? [], // ensure always array
      averageRating: this.averageRating(book), // ensure always number
    }));
    this.books.set(this.refineData(booksWithAverage));
  }

  protected refineData(data: BookWithAverage[]): BookWithAverage[] {
    const { predicate, order } = this.sortState();
    return predicate && order ? data.sort(this.sortService.startSort({ predicate, order })) : data;
  }

  protected fillComponentAttributesFromResponseBody(data: IBook[] | null): IBook[] {
    return data ?? [];
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    const { currentSearch } = this;

    this.isLoading = true;
    const queryObject: any = {
      eagerload: true,
      query: currentSearch,
      sort: this.sortService.buildSortParam(this.sortState()),
    };
    if (this.currentSearch && this.currentSearch !== '') {
      return this.bookService.search(queryObject).pipe(tap(() => (this.isLoading = false)));
    }
    return this.bookService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(sortState: SortState, currentSearch?: string): void {
    const queryParamsObj = {
      search: currentSearch,
      sort: this.sortService.buildSortParam(sortState),
    };

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    });
  }
}
