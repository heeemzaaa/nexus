import { inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ConfirmDialogComponent } from '../components/confirm-dialog/confirm-dialog.component';
import { DialogService } from '../../core/services/dialog.service';

export interface ConfirmDialogData {
    title?: string;
    message?: string;
    confirmText?: string;
    cancelText?: string;
}

const defaultConfirmData = {
    title: "Confirmation",
    message: "Are you sure you want to perform this action?",
    confirmText: "Confirm",
    cancelText: "Cancel"
}

export function Confirmable(confirmData: ConfirmDialogData = defaultConfirmData) {
    return function (target: Object, propertyKey: string, descriptor: PropertyDescriptor) {
        const originalMethod = descriptor.value;

        descriptor.value = async function (...args: any) {
            DialogService.getInstance()?.openDialog(confirmData, ConfirmDialogComponent).subscribe((validation) => {
                if (validation) {
                    originalMethod.apply(this, args);
                }
            });
        };

        return descriptor;
    };
}