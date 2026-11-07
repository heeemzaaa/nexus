import { Component, input } from '@angular/core';

@Component({
  selector: 'app-success-popup',
  imports: [],
  templateUrl: './success-popup.component.html',
  styleUrl: './success-popup.component.scss'
})
export class SuccessPopupComponent {
  message = input.required();
}
