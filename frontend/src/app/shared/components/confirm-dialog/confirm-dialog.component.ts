import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogClose } from '@angular/material/dialog';
import { ConfirmDialogData } from '../../decorators/confirmable.decorator';

@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogClose],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss'
})
export class ConfirmDialogComponent {
  data: ConfirmDialogData = inject(MAT_DIALOG_DATA)
}
