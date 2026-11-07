import { Component, inject } from '@angular/core';
import { OrderService } from '../../../../core/services/order.service';

@Component({
  selector: 'app-order-paginator',
  imports: [],
  templateUrl: './order-paginator.component.html',
  styleUrl: './order-paginator.component.scss'
})
export class OrderPaginatorComponent {
  orderService = inject(OrderService);
}
