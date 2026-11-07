import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API } from '../config/api';
import { CartItem } from '../interfaces/cart-item.interface';

@Injectable({
  providedIn: 'root',
})
export class ShoppingCartService {
  private http = inject(HttpClient);

  createItem(productId: string, quantity: number) {
    return this.http.post<CartItem>(API.CREATE_ITEM, { product_id: productId, quantity })
  }

  fetchItems() {
    return this.http.get<CartItem[]>(API.GET_ITEMS)
  }

  updateItem(itemId: string, productId: string, quantity: number) {
    return this.http.put(API.UPDATE_ITEM + "/" + itemId, { product_id: productId, quantity })
  }

  submitDelete(itemId: string) {
    return this.http.delete(API.DELETE_ITEM + `/${itemId}`)
  }

  submitCheckout() {
    return this.http.post(API.CREATE_ORDER, {})
  }
} 
