import { Component, EventEmitter, HostListener, inject, input, Output, signal } from '@angular/core';
import { ProductService } from '../../../core/services/product.service';
import { PopupService } from '../../../core/services/popup.service';
import { Confirmable } from '../../decorators/confirmable.decorator';
import { Product } from '../../../core/interfaces/product.interface';

@Component({
  selector: 'app-product-options',
  imports: [],
  templateUrl: './product-options.component.html',
  styleUrl: './product-options.component.scss'
})
export class ProductOptionsComponent {
  private productService = inject(ProductService)
  private popupService = inject(PopupService)

  @Output()
  hide = new EventEmitter()

  @Output()
  showUpdateProduct = new EventEmitter();

  productId = input.required<string>()

  updateProduct() {
    this.showUpdateProduct.emit()
  }

  @Confirmable()
  deleteProduct() {
    this.productService.submitDeleteProduct(this.productId()).subscribe(res => {
      this.popupService.showSuccess(res.message)
      this.productService.productDelete(res.productId)
    })
  }

  @HostListener('document:click')
  hideOptions() {
    this.hide.emit()
  }
}
