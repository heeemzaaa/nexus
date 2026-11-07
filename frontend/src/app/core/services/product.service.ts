import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal, computed } from '@angular/core';
import { Product } from '../interfaces/product.interface';
import { API } from '../config/api';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private http = inject(HttpClient);

  private readonly pageSize = 6;
  private readonly _page = signal(0);
  private readonly _products = signal<Product[]>([]);
  private readonly _loading = signal(false);
  searchValue = signal('');
  sortBy = signal('id');

  // Exposed signals
  readonly page = computed(() => this._page());
  readonly products = computed(() => this._products());
  readonly loading = computed(() => this._loading());
  readonly isFirstPage = computed(() => this._page() === 0);
  readonly isLastPage = computed(() => this._products().length < this.pageSize);

  loadProducts(userId?: string) {
    this._loading.set(true);
    const params = new HttpParams()
      .set('page', this._page().toString())
      .set('size', this.pageSize.toString())
      .set('search', this.searchValue())
      .set('sortedBy', this.sortBy());

    this.http
      .get<
        Product[]
      >(!userId ? API.GET_POSTS : `${API.GET_PROFILE_POSTS}/${userId}`, { params })
      .subscribe({
        next: (products) => {
          this._products.set(products);
          this._loading.set(false);
        },
        error: (err) => {
          this._loading.set(false);
          throw err;
        },
      });
  }

  nextPage(userId?: string) {
    if (!this.isLastPage()) {
      this._page.update((p) => p + 1);
      this.loadProducts(userId);
    }
  }

  previousPage(userId?: string) {
    if (!this.isFirstPage()) {
      this._page.update((p) => Math.max(0, p - 1));
      this.loadProducts(userId);
    }
  }

  productUnshift(product: Product) {
    if (!this.isFirstPage()) return;
    this._products.set(this._products().filter((p) => p.id !== product.id));
    this._products.update((p) => [
      product,
      ...(p.length === this.pageSize ? p.splice(0, p.length - 1) : p),
    ]);
  }

  productDelete(productId: string) {
    const deletedProduct: Product = {
      id: productId,
      name: '',
      description: '',
      price: 0,
      quantity: 0,
      user_id: '',
    };

    this._products.set(
      this._products().map((p) => {
        if (p.id === productId) {
          return deletedProduct;
        }

        return p;
      }),
    );
  }

  productUpdate(product: Product) {
    this._products.set(
      this._products().map((p) => {
        if (p.id === product.id) {
          return product;
        }

        return p;
      }),
    );
  }

  resetPage() {
    this._page.set(0);
  }

  resetProducts() {
    this._products.set([]);
    this.searchValue.set('');
    this.sortBy.set('id');
  }

  submitProduct(product: any, productId?: string) {
    return !productId
      ? this.http.post<Product>(API.CREATE_PRODUCT, product)
      : this.http.put<Product>(API.UPDATE_PRODUCT + '/' + productId, product);
  }

  submitDeleteProduct(productId: string) {
    return this.http.delete<{ productId: string; message: string }>(
      API.DELETE_PRODUCT + '/' + productId,
    );
  }
}
