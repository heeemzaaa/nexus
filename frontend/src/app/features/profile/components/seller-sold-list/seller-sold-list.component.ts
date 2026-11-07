import { Component, inject, OnDestroy, OnInit, signal, computed } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { OrderService } from '../../../../core/services/order.service';
import { SoldProduct } from '../../../../core/interfaces/sold-product.interface';

@Component({
  selector: 'app-seller-sold-list',
  imports: [CurrencyPipe],
  templateUrl: './seller-sold-list.component.html',
  styleUrl: './seller-sold-list.component.scss'
})
export class SellerSoldListComponent implements OnInit, OnDestroy {
  private orderService = inject(OrderService);

  allSold = signal<SoldProduct[]>([]);
  loading = signal<boolean>(false);

  // 2 tabs: 'ORDERED' and 'CANCELLED'
  activeTab = signal<'ORDERED' | 'CANCELLED'>('ORDERED');
  searchQuery = signal<string>('');

  // Live counts for tabs
  orderedCount = computed(() => {
    return this.allSold().filter(p => p.orderStatus !== 'CANCELLED' && p.orderStatus !== 'CANCELED' && p.orderStatus !== 'FAILED').length;
  });

  cancelledCount = computed(() => {
    return this.allSold().filter(p => p.orderStatus === 'CANCELLED' || p.orderStatus === 'CANCELED' || p.orderStatus === 'FAILED').length;
  });

  // Client-side analytics: Total revenue earned (active sales only)
  totalRevenue = computed(() => {
    return this.allSold()
      .filter(p => p.orderStatus !== 'CANCELLED' && p.orderStatus !== 'CANCELED' && p.orderStatus !== 'FAILED')
      .reduce((sum, p) => sum + (p.price * p.quantity), 0);
  });

  // Client-side analytics: Total quantity of items sold (active sales only)
  totalItemsSold = computed(() => {
    return this.allSold()
      .filter(p => p.orderStatus !== 'CANCELLED' && p.orderStatus !== 'CANCELED' && p.orderStatus !== 'FAILED')
      .reduce((sum, p) => sum + p.quantity, 0);
  });

  // Client-side computed filtered sold products
  filteredSold = computed(() => {
    const sold = this.allSold();
    const tab = this.activeTab();
    const query = this.searchQuery().toLowerCase().trim();

    // 1. Filter by Tab
    let result = sold;
    if (tab === 'ORDERED') {
      result = sold.filter(p => p.orderStatus !== 'CANCELLED' && p.orderStatus !== 'CANCELED' && p.orderStatus !== 'FAILED');
    } else if (tab === 'CANCELLED') {
      result = sold.filter(p => p.orderStatus === 'CANCELLED' || p.orderStatus === 'CANCELED' || p.orderStatus === 'FAILED');
    }

    // 2. Filter by search query (match by product name, order ID, or total price)
    if (query) {
      result = result.filter(p => 
        p.productName.toLowerCase().includes(query) ||
        p.orderId.toLowerCase().includes(query) ||
        (p.price * p.quantity).toString().includes(query)
      );
    }

    return result;
  });

  ngOnInit(): void {
    this.fetchSoldProducts();
  }

  ngOnDestroy(): void {
    this.allSold.set([]);
  }

  fetchSoldProducts() {
    this.loading.set(true);
    this.orderService.getSellerOrders().subscribe({
      next: (sold) => {
        this.allSold.set(sold);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        throw err;
      }
    });
  }

  setTab(tab: 'ORDERED' | 'CANCELLED') {
    this.activeTab.set(tab);
  }
}
