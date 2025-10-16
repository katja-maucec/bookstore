import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RefreshService {
  private _refresh$ = new Subject<void>();

  // Called by detail component when reviews change
  notifyRefresh(): void {
    this._refresh$.next();
  }

  // Subscribed to by book list
  onRefresh(): Observable<void> {
    return this._refresh$.asObservable();
  }
}
