import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BookReviewComponent } from './book-review.component';

describe('BookReviewComponent', () => {
  let component: BookReviewComponent;
  let fixture: ComponentFixture<BookReviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookReviewComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BookReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
