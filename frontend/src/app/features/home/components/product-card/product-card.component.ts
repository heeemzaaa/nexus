import {
  Component,
  input,
  computed,
  signal,
  inject,
  CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core';
import { Product } from '../../../../core/interfaces/product.interface';
import { CurrencyPipe } from '@angular/common';
import { ProductOptionsComponent } from '../../../../shared/components/product-options/product-options.component';
import { AuthStateService } from '../../../../core/services/auth-state.service';
import { CreateProductComponent } from '../../../../shared/components/create-product/create-product.component';
import '@tailwindplus/elements';
import { Router } from '@angular/router';
import { PopupService } from '../../../../core/services/popup.service';
import { ShoppingCartService } from '../../../../core/services/shopping-cart.service';

@Component({
  selector: 'app-product-card',
  imports: [CurrencyPipe, ProductOptionsComponent, CreateProductComponent],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ProductCardComponent {
  private popupService = inject(PopupService)
  private router = inject(Router)
  private shoppingCartService = inject(ShoppingCartService)
  private popup = inject(PopupService)

  product = input.required<Product>();
  currentUser = inject(AuthStateService);
  selectedImageIndex = signal(0);
  isAddedToShoppingCart = signal(false)

  currentDrawerImage = computed(
    () => this.drawerImages()[this.selectedImageIndex()],
  );

  drawerImages = computed(() => {
    const files = this.product().files || [];
    if (files.length === 0) {
      return [
        `https://placehold.co/600x400/222222/FFFFFF?text=${encodeURIComponent(this.product().name)}`,
      ];
    }
    return files.slice(0, 5); // Max 5 images
  });

  isVisibleOptions = signal<boolean>(false);
  isVisibleUpdateProduct = signal<boolean>(false);

  productImage = computed(() => {
    const files = this.product().files;
    return files && files.length > 0
      ? files[0]
      : `https://placehold.co/600x400/222222/FFFFFF?text=${encodeURIComponent(this.product().name)}`;
  });

  selectImage(index: number) {
    this.selectedImageIndex.set(index);
  }

  isOutOfStock = computed(() => this.product().quantity === 0);

  navigateToProfile() {
    if (!this.currentUser.isAuthenticated()) {
      this.popupService.showError('You should login to access seller profile.')
      return
    }

    this.router.navigate(['/profile', this.product().user_id])
  }

  addItem(productId: string) {
    this.shoppingCartService.createItem(productId, 1).subscribe(res => {
      this.popup.showSuccess("Product added successfully.")
      this.isAddedToShoppingCart.set(true)
    })
  }
}
