import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PopupService {

  private successSubject = new Subject<string>();
  success$ = this.successSubject.asObservable();

  private errorSubject = new Subject<string>();
  error$ = this.errorSubject.asObservable();

  showError(message: string) {
    this.errorSubject.next(message);
  }

  showSuccess(message: string) {
    this.successSubject.next(message);
  }
}
