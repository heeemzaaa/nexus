import { ErrorHandler, inject, Injectable } from '@angular/core';
import { PopupService } from './popup.service';

@Injectable({
  providedIn: 'root'
})
export class GlobalErrorHandlerService implements ErrorHandler {

  private popupService = inject(PopupService)

  handleError(error: any): void {
    // console.error(error)
    let message = 'Something went wrong';

    if (error?.error?.message) {
      message = error.error.message
    } else if (error?.message) {
      message = error.message;
    }

    this.popupService.showError(message);
  }

}
