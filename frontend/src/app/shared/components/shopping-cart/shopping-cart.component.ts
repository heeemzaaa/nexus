import { Component, computed, EventEmitter, inject, OnDestroy, OnInit, Output, signal } from '@angular/core';
import { ShoppingCartService } from '../../../core/services/shopping-cart.service';
import { CartItem } from '../../../core/interfaces/cart-item.interface';
import { PopupService } from '../../../core/services/popup.service';
import { Confirmable } from '../../decorators/confirmable.decorator';

@Component({
  selector: 'app-shopping-cart',
  imports: [],
  templateUrl: './shopping-cart.component.html',
  styleUrl: './shopping-cart.component.scss',
})
export class ShoppingCartComponent implements OnInit, OnDestroy {
  @Output()
  closeCart = new EventEmitter()

  private shoppingCartService = inject(ShoppingCartService)
  private popup = inject(PopupService)

  items = signal<CartItem[]>([]);

  totalPrice = computed(() => {
    let total = 0
    this.items().forEach(item => {
      total += item.product.price * item.item_quantity
    })

    return total
  })

  ngOnInit(): void {
    document.body.classList.add('overflow-hidden');
    this.shoppingCartService.fetchItems().subscribe(res => {
      this.items.set(res)
      console.log(this.items());
    })
  }

  ngOnDestroy(): void {
    document.body.classList.remove('overflow-hidden');
  }

  updateItem(currentItem: CartItem, increment: boolean) {
    this.items.set(this.items().map(item => {
      if (currentItem.id != item.id) return item

      if (increment) {
        item.item_quantity++
      } else {
        item.item_quantity--
      }
      return item
    }))

    this.shoppingCartService.updateItem(currentItem.id, currentItem.product_id, currentItem.item_quantity).subscribe()
  }

  @Confirmable()
  deleteItem(itemId: string) {
    this.shoppingCartService.submitDelete(itemId).subscribe()
    this.items.set(this.items().filter(item => item.id != itemId))
  }

  checkout() {
    this.shoppingCartService.submitCheckout().subscribe({
      next: res => {
        this.popup.showSuccess("Checkout done successfully.")
        this.closeCart.emit()
      },
      error: err => {
          this.shoppingCartService.fetchItems().subscribe(res => {
            this.items.set(res)
          })
        
        throw err
      }
    })
  }

  itemImage(item: CartItem) {
    const files = item.product.files;
    return files && files.length > 0
      ? files[0]
      : `https://placehold.co/600x400/222222/FFFFFF?text=${encodeURIComponent(item.product.name)}`;
  }
}
