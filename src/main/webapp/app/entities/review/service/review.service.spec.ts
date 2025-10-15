import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IReview } from '../review.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../review.test-samples';

import { RestReview, ReviewService } from './review.service';

const requireRestSample: RestReview = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('Review Service', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;
  let expectedResult: IReview | IReview[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Review', () => {
      const review = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(review).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Review', () => {
      const review = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(review).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Review', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Review', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Review', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Review', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addReviewToCollectionIfMissing', () => {
      it('should add a Review to an empty array', () => {
        const review: IReview = sampleWithRequiredData;
        expectedResult = service.addReviewToCollectionIfMissing([], review);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(review);
      });

      it('should not add a Review to an array that contains it', () => {
        const review: IReview = sampleWithRequiredData;
        const reviewCollection: IReview[] = [
          {
            ...review,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addReviewToCollectionIfMissing(reviewCollection, review);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Review to an array that doesn't contain it", () => {
        const review: IReview = sampleWithRequiredData;
        const reviewCollection: IReview[] = [sampleWithPartialData];
        expectedResult = service.addReviewToCollectionIfMissing(reviewCollection, review);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(review);
      });

      it('should add only unique Review to an array', () => {
        const reviewArray: IReview[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const reviewCollection: IReview[] = [sampleWithRequiredData];
        expectedResult = service.addReviewToCollectionIfMissing(reviewCollection, ...reviewArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const review: IReview = sampleWithRequiredData;
        const review2: IReview = sampleWithPartialData;
        expectedResult = service.addReviewToCollectionIfMissing([], review, review2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(review);
        expect(expectedResult).toContain(review2);
      });

      it('should accept null and undefined values', () => {
        const review: IReview = sampleWithRequiredData;
        expectedResult = service.addReviewToCollectionIfMissing([], null, review, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(review);
      });

      it('should return initial array if no Review is added', () => {
        const reviewCollection: IReview[] = [sampleWithRequiredData];
        expectedResult = service.addReviewToCollectionIfMissing(reviewCollection, undefined, null);
        expect(expectedResult).toEqual(reviewCollection);
      });
    });

    describe('compareReview', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareReview(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 21337 };
        const entity2 = null;

        const compareResult1 = service.compareReview(entity1, entity2);
        const compareResult2 = service.compareReview(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 21337 };
        const entity2 = { id: 8996 };

        const compareResult1 = service.compareReview(entity1, entity2);
        const compareResult2 = service.compareReview(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 21337 };
        const entity2 = { id: 21337 };

        const compareResult1 = service.compareReview(entity1, entity2);
        const compareResult2 = service.compareReview(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
