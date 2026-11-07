import { Component, EventEmitter, input, Output } from '@angular/core';

@Component({
  selector: 'app-error-popup',
  imports: [],
  templateUrl: './error-popup.component.html',
  styleUrl: './error-popup.component.scss'
})
export class ErrorPopupComponent {
  message = input.required();
}
