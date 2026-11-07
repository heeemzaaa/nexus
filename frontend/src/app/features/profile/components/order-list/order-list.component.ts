import { Component, inject, OnDestroy, OnInit, signal, computed } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { OrderService } from '../../../../core/services/order.service';
import { PopupService } from '../../../../core/services/popup.service';
import { Order } from '../../../../core/interfaces/order.interface';
import { OrderItem } from '../../../../core/interfaces/order-item.interface';
import { of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-order-list',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.scss'
})
export class OrderListComponent implements OnInit, OnDestroy {
  private orderService = inject(OrderService);
  private popupService = inject(PopupService);

  allOrders = signal<Order[]>([]);
  loading = signal<boolean>(false);
  
  // 2 tabs: 'ORDERED' and 'CANCELED'
  activeTab = signal<'ORDERED' | 'CANCELED'>('ORDERED');
  searchQuery = signal<string>('');

  // Live counts for tabs
  orderedCount = computed(() => {
    return this.allOrders().filter(o => o.status !== 'CANCELLED' && o.status !== 'CANCELED' && o.status !== 'FAILED').length;
  });

  canceledCount = computed(() => {
    return this.allOrders().filter(o => o.status === 'CANCELLED' || o.status === 'CANCELED' || o.status === 'FAILED').length;
  });

  // Client-side analytics: Total spent by client (active orders only)
  totalSpent = computed(() => {
    return this.allOrders()
      .filter(o => o.status !== 'CANCELLED' && o.status !== 'CANCELED' && o.status !== 'FAILED')
      .reduce((sum, o) => sum + o.total, 0);
  });

  // Client-side computed filtered orders
  filteredOrders = computed(() => {
    const orders = this.allOrders();
    const tab = this.activeTab();
    const query = this.searchQuery().toLowerCase().trim();

    // 1. Filter by Tab
    let result = orders;
    if (tab === 'ORDERED') {
      result = orders.filter(o => o.status !== 'CANCELLED' && o.status !== 'CANCELED' && o.status !== 'FAILED');
    } else if (tab === 'CANCELED') {
      result = orders.filter(o => o.status === 'CANCELLED' || o.status === 'CANCELED' || o.status === 'FAILED');
    }

    // 2. Filter by search query (match by order ID, status, or total price)
    if (query) {
      result = result.filter(o => 
        o.id.toLowerCase().includes(query) ||
        o.status.toLowerCase().includes(query) ||
        o.total.toString().includes(query)
      );
    }

    return result;
  });

  // Dynamically calculate the total of items
  calculatedTotal = computed(() => {
    return this.orderItems().reduce((acc, item) => acc + (item.checkoutPrice * item.quantity), 0);
  });

  // Sidebar details state
  selectedOrder = signal<Order | null>(null);
  orderItems = signal<OrderItem[]>([]);
  loadingItems = signal<boolean>(false);

  ngOnInit(): void {
    this.fetchOrders();
  }

  ngOnDestroy(): void {
    this.allOrders.set([]);
  }

  fetchOrders() {
    this.loading.set(true);
    this.orderService.loadOrders().subscribe({
      next: (orders) => {
        this.allOrders.set(orders);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        throw err;
      }
    });
  }

  setTab(tab: 'ORDERED' | 'CANCELED') {
    this.activeTab.set(tab);
  }

  cancelOrder(orderId: string) {
    this.orderService.cancelOrder(orderId).subscribe({
      next: (updatedOrder) => {
        this.popupService.showSuccess('Order canceled successfully.');
        this.allOrders.update(orders => 
          orders.map(o => o.id === orderId ? { ...o, status: updatedOrder.status } : o)
        );
        // If the cancelled order is currently open in details, update its status
        if (this.selectedOrder()?.id === orderId) {
          this.selectedOrder.update(o => o ? { ...o, status: updatedOrder.status } : null);
        }
      },
      error: (err) => {
        this.popupService.showError(err.error?.message || 'Failed to cancel the order.');
      }
    });
  }

  redoOrder(orderId: string) {
    this.orderService.redoOrder(orderId).subscribe({
      next: (updatedOrder) => {
        this.popupService.showSuccess('Order placed again successfully.');
        this.allOrders.update(orders => 
          orders.map(o => o.id === orderId ? { ...o, status: updatedOrder.status } : o)
        );
        // If the redone order is currently open in details, update its status
        if (this.selectedOrder()?.id === orderId) {
          this.selectedOrder.update(o => o ? { ...o, status: updatedOrder.status } : null);
        }
      },
      error: (err) => {
        this.popupService.showError(err.error?.message || 'Failed to redo the order.');
      }
    });
  }

  openOrderDetails(order: Order) {
    this.selectedOrder.set(order);
    this.orderItems.set([]);
    this.loadingItems.set(true);

    this.orderService.getOrderItems(order.id).pipe(
      switchMap(items => {
        if (!items || items.length === 0) {
          return of({ items: [], products: [] });
        }
        const productIds = Array.from(new Set(items.map(item => item.productId)));
        return this.orderService.getProductsBatch(productIds).pipe(
          switchMap(products => of({ items, products })),
          catchError(() => of({ items, products: [] }))
        );
      }),
      catchError(err => {
        this.popupService.showError('Failed to load order items.');
        this.loadingItems.set(false);
        throw err;
      })
    ).subscribe(({ items, products }) => {
      const productsMap = new Map(products.map(p => [p.id, p]));
      const enrichedItems = items.map(item => ({
        ...item,
        product: productsMap.get(item.productId)
      }));
      this.orderItems.set(enrichedItems);
      this.loadingItems.set(false);
    });
  }

  closeDetails() {
    this.selectedOrder.set(null);
    this.orderItems.set([]);
  }
}
