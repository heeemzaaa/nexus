import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Order } from '../interfaces/order.interface';
import { OrderItem } from '../interfaces/order-item.interface';
import { Product } from '../interfaces/product.interface';
import { SoldProduct } from '../interfaces/sold-product.interface';
import { API } from '../config/api';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private http = inject(HttpClient);

  loadOrders() {
    // Retrieve all orders by specifying a large size
    const params = new HttpParams()
      .set('page', '0')
      .set('size', '1000');

    return this.http.get<Order[]>(API.GET_ORDERS, { params });
  }

  cancelOrder(orderId: string) {
    return this.http.put<Order>(`${API.GET_ORDERS}/${orderId}/cancel`, {});
  }

  redoOrder(orderId: string) {
    return this.http.post<Order>(`${API.GET_ORDERS}/${orderId}/redo`, {});
  }

  getOrderItems(orderId: string) {
    return this.http.get<OrderItem[]>(`${API.GET_ORDERS}/${orderId}/items`);
  }

  getProductsBatch(productIds: string[]) {
    const url = API.GET_ORDERS.replace('/orders', '/products/carts');
    return this.http.post<Product[]>(url, productIds);
  }

  getSellerOrders(status?: string) {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<SoldProduct[]>(`${API.GET_ORDERS}/seller`, { params });
  }
}
