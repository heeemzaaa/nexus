import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PopupService } from './core/services/popup.service';
import { ErrorPopupComponent } from "./shared/components/error-popup/error-popup.component";
import { SuccessPopupComponent } from "./shared/components/success-popup/success-popup.component";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ErrorPopupComponent, SuccessPopupComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {

  private popupService = inject(PopupService)
  messageError = signal<string | null>(null)
  messageSucces = signal<string | null>(null)

  ngOnInit(): void {
    this.popupService.error$.subscribe(message => {
      if (!this.messageError()) {
        this.messageError.set(message)
        setTimeout(() => {
          this.messageError.set(null)
        }, 4000);
      }
    });

    this.popupService.success$.subscribe(message => {
      if (!this.messageSucces()) {
        this.messageSucces.set(message)
        setTimeout(() => {
          this.messageSucces.set(null)
        }, 4000);
      }
    });
  }
}
