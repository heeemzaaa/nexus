import { Component, inject, input, OnDestroy, OnInit } from '@angular/core';
import { ProductCardComponent } from '../product-card/product-card.component';
import { ProductService } from '../../../../core/services/product.service';

@Component({
  selector: 'app-product-list',
  imports: [ProductCardComponent],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss',
})
export class ProductListComponent implements OnInit, OnDestroy {
  productService = inject(ProductService);
  products = this.productService.products;
  loading = this.productService.loading;

  // if userId is undefined means get "/feed" products  
  // else, get products by profileId
  userId = input<string | undefined>(undefined)

  ngOnInit(): void {
    this.productService.resetPage();
    this.productService.loadProducts(this.userId())
  }

  ngOnDestroy(): void {
    this.productService.resetProducts()
  }
}
