import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faStar as farStar } from '@fortawesome/free-regular-svg-icons';
import { faStar } from '@fortawesome/free-solid-svg-icons';
import { IBook } from 'app/entities/book/book.model';
import { Review, NewReview } from 'app/entities/review/review.model';
import { ReviewService } from 'app/entities/review/service/review.service';
import { BookService } from 'app/entities/book/service/book.service';
import { RefreshService } from 'app/shared/refresh.service';

@Component({
  selector: 'app-book-review',
  templateUrl: './book-review.component.html',
  standalone: true,
  imports: [FormsModule, FontAwesomeModule, CommonModule],
})
export class BookReviewComponent implements OnInit {
  faStar = faStar;
  farStar = farStar;

  book?: IBook;
  review: Review = new Review(); // class allows id to be null
  submitted = false;

  hoveredRating: number | null = null;

  private reviewService = inject(ReviewService);
  private bookService = inject(BookService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private refreshService = inject(RefreshService);

  ngOnInit(): void {
    const bookId = this.route.snapshot.paramMap.get('id');
    if (bookId) {
      this.bookService.find(+bookId).subscribe({
        next: resp => {
          this.book = resp.body ?? undefined;
        },
        error: err => {
          console.error('Error loading book:', err);
        },
      });
    }
  }

  submitReview(): void {
    this.submitted = true;

    if (!this.review.rating || !this.review.comment) {
      return; // basic form validation
    }

    if (this.book) {
      this.review.book = { id: this.book.id } as any;
      this.review.createdAt = new Date() as any;

      const newReview: NewReview = this.review.toNewReview();

      this.reviewService.create(newReview).subscribe({
        next: () => {
          alert('Review submitted successfully!');
          // Trigger refresh so the book detail page reloads data
          this.refreshService.notifyRefresh();
          this.router.navigate(['/book', this.book!.id, 'view'], { replaceUrl: true });
        },
        error: err => {
          console.error('Error submitting review:', err.status, err.error);
          alert('Something went wrong when submitting the review.');
        },
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/book']);
  }
}
