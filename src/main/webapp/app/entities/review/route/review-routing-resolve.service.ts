import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IReview } from '../review.model';
import { ReviewService } from '../service/review.service';

const reviewResolve = (route: ActivatedRouteSnapshot): Observable<null | IReview> => {
  const id = route.params.id;
  if (id) {
    return inject(ReviewService)
      .find(id)
      .pipe(
        mergeMap((review: HttpResponse<IReview>) => {
          if (review.body) {
            return of(review.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default reviewResolve;
