import { Component, inject, OnDestroy } from '@angular/core';
import { ProductListComponent } from '../components/product-list/product-list.component';
import { PaginatorComponent } from '../components/paginator/paginator.component';
import { ProductService } from '../../../core/services/product.service';
import { debounceTime, Subject, Subscription } from 'rxjs';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-feed',
  imports: [ProductListComponent, PaginatorComponent, ReactiveFormsModule],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.scss',
})
export class FeedComponent implements OnDestroy {
  private searchSubject = new Subject();
  private searchSubscription: Subscription;

  private productService = inject(ProductService);

  // searchForm = new FormGroup({
  //   search: new FormControl(''),
  //   sort: new FormControl('id')
  // })

  searchIpt = new FormControl('');
  sortBy = new FormControl('id');

  constructor() {
    this.searchSubscription = this.searchSubject
      .pipe(debounceTime(300))
      .subscribe((value) => {
        this.search(value);
      });
  }

  ngOnDestroy(): void {
    this.searchSubscription.unsubscribe();
  }

  handleSearch() {
    this.searchSubject.next(this.searchIpt.value);
  }

  handleChange() {
    this.productService.resetPage();
    this.productService.resetProducts();
    this.productService.searchValue.set(this.searchIpt.value ?? '');
    this.productService.sortBy.set(this.sortBy.value ?? 'id');
    this.productService.loadProducts();
  }

  private search(value: any) {
    this.productService.resetPage();
    this.productService.resetProducts();
    this.productService.searchValue.set(value);
    this.productService.sortBy.set(this.sortBy.value ?? 'id');
    this.productService.loadProducts();
  }
}
