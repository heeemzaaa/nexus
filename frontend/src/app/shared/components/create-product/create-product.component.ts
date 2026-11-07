import { Component, EventEmitter, HostListener, inject, input, model, OnDestroy, OnInit, Output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../../../core/services/product.service';
import { MediaService } from '../../../core/services/media.service';
import { Confirmable } from '../../decorators/confirmable.decorator';
import { NgClass } from '@angular/common';
import { PopupService } from '../../../core/services/popup.service';
import { finalize } from 'rxjs';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { ActivatedRoute } from '@angular/router';
import { Product } from '../../../core/interfaces/product.interface';

@Component({
  selector: 'app-create-product',
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './create-product.component.html',
  styleUrl: './create-product.component.scss'
})
export class CreateProductComponent implements OnInit, OnDestroy {
  private productService = inject(ProductService)
  private mediaService = inject(MediaService)
  private popupService = inject(PopupService)
  private currentUser = inject(AuthStateService)
  private activatedRoute = inject(ActivatedRoute);

  @Output()
  close = new EventEmitter()

  media = signal<{ url: string, file: File }[]>([])
  mediaError = signal<string>('')
  creatingProduct = signal<boolean>(false)

  product = model<Product | null>()

  productForm = new FormGroup({
    name: new FormControl(''),
    description: new FormControl(''),
    price: new FormControl<any>(''),
    quantity: new FormControl('')
  })

  ngOnInit(): void {
    document.body.classList.add('overflow-hidden');
    if (!this.product()) return;

    this.name.setValue(this.product()?.name || '')
    this.description.setValue(this.product()?.description || '')
    this.price.setValue(this.product()?.price || '')
    this.quantity.setValue(this.product()?.quantity.toString() || '')
  }

  ngOnDestroy(): void {
    document.body.classList.remove('overflow-hidden');
  }

  onSubmit() {
    if (this.creatingProduct()) return;

    this.mediaError.set('')

    if (!this.name.value?.trim()) this.name.setErrors({ required: "Product name cannot be empty" })
    if (!this.description.value?.trim()) this.description.setErrors({ required: "Product description cannot be empty" })
    if (!this.price.value) this.price.setErrors({ required: "Product price cannot be empty" })
    if (!this.quantity.value) this.quantity.setErrors({ required: "Product quantity cannot be empty" })

    if (this.productForm.invalid) return;

    if (this.price.value && !isNaN(this.price.value)) {
      this.price.setValue(parseFloat(this.price.value).toFixed(2))
    }

    if (this.media().length === 0 && !this.product()) {
      this.mediaError.set("Product images must be between 1 and 5 image")
      return
    };

    this.creatingProduct.set(true)
    this.productService.submitProduct(this.productForm.value, this.product()?.id).subscribe({
      next: res => {
        const files: File[] = this.media().map(m => m.file)
        if (files.length === 0) {
          this.showProduct(res, this.product()?.files || [])
          return
        };
        this.mediaService.submitMedia(files, (this.product()?.id ? 'UPDATE_PRODUCT' : 'PRODUCT'), res.id).pipe(
          finalize(() => {
            this.creatingProduct.set(false)
          })
        ).subscribe({
          next: (mediaResponse) => {
            this.showProduct(res, mediaResponse.files)
          },
          error: e => {
            this.showProduct(res, this.product()?.files || [], true)
            this.product.set(res)
            this.productForm.setErrors({ 'error': e.error.message })
            throw e
          }
        })
      },
      error: err => {
        this.creatingProduct.set(false)
        let fieldErrors = err.error?.validationErrors
        if (fieldErrors) {
          this.productForm.setErrors(fieldErrors)
          return;
        }

        throw err
      }
    })
  }

  onMediaChange(event: Event) {
    this.mediaError.set('')
    this.productForm.setErrors(null)
    const ipt = event.target as HTMLInputElement

    if (this.media().length === 5) {
      ipt.value = ''
      this.popupService.showError("The product cannot exceed 5 images.")
      return;
    };

    if (!ipt.files || ipt.files?.length == 0) return;
    const file = ipt.files[0]
    ipt.value = ''

    const MAX_SIZE = 2 * 1024 * 1024;
    if (file.size > MAX_SIZE) {
      this.popupService.showError("Image size must not exceed 2MB.")
      return;
    }

    const objectUrl = URL.createObjectURL(file)
    this.media().push({ url: objectUrl, file: file })
  }

  removeMedia(index: number) {
    this.productForm.setErrors(null)
    URL.revokeObjectURL(this.media()[index].url)
    this.media().splice(index, 1)
  }

  @HostListener('click')
  closeModal() {
    let needConfirm = false
    Object.values(this.productForm.value).forEach(value => {
      if (value) {
        needConfirm = true
        return
      }
    })

    if (this.media().length > 0) {
      needConfirm = true
    }

    if (!needConfirm) {
      this.close.emit()
    } else {
      this.closeModalWithConfirmation()
    }
  }

  @Confirmable()
  private closeModalWithConfirmation() {
    this.close.emit()
  }

  private showProduct(product: Product, media: string[], mediaError: boolean = false) {
    if (media.length >= 0 && !mediaError) {
      this.close.emit()
      let successMessage = this.product() ? 'Product updated successfully.' : "Product created successfully."
      this.popupService.showSuccess(successMessage)
      if (media.length > 0) product.status = 'ACTIVE'
    };

    const profileId = this.activatedRoute.snapshot.paramMap.get('id');
    if (profileId === this.currentUser.currentUser()?.id) {
      if (this.product()) {
        this.productService.productUpdate({
          ...product,
          user_infos: this.currentUser.currentUser() || undefined,
          files: media
        })
      } else {
        this.productService.productUnshift({
          ...product,
          user_infos: this.currentUser.currentUser() || undefined,
          files: media
        })
      }
    }
  }

  get name() {
    return this.productForm.controls.name
  }

  get description() {
    return this.productForm.controls.description
  }

  get price() {
    return this.productForm.controls.price
  }

  get quantity() {
    return this.productForm.controls.quantity
  }

}
