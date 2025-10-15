import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../review.test-samples';

import { ReviewFormService } from './review-form.service';

describe('Review Form Service', () => {
  let service: ReviewFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReviewFormService);
  });

  describe('Service methods', () => {
    describe('createReviewFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createReviewFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            rating: expect.any(Object),
            comment: expect.any(Object),
            createdAt: expect.any(Object),
            user: expect.any(Object),
            book: expect.any(Object),
          }),
        );
      });

      it('passing IReview should create a new form with FormGroup', () => {
        const formGroup = service.createReviewFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            rating: expect.any(Object),
            comment: expect.any(Object),
            createdAt: expect.any(Object),
            user: expect.any(Object),
            book: expect.any(Object),
          }),
        );
      });
    });

    describe('getReview', () => {
      it('should return NewReview for default Review initial value', () => {
        const formGroup = service.createReviewFormGroup(sampleWithNewData);

        const review = service.getReview(formGroup) as any;

        expect(review).toMatchObject(sampleWithNewData);
      });

      it('should return NewReview for empty Review initial value', () => {
        const formGroup = service.createReviewFormGroup();

        const review = service.getReview(formGroup) as any;

        expect(review).toMatchObject({});
      });

      it('should return IReview', () => {
        const formGroup = service.createReviewFormGroup(sampleWithRequiredData);

        const review = service.getReview(formGroup) as any;

        expect(review).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IReview should not enable id FormControl', () => {
        const formGroup = service.createReviewFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewReview should disable id FormControl', () => {
        const formGroup = service.createReviewFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
