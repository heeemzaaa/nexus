import { Product } from "./product.interface";

export interface OrderItem {
  id: string;
  orderId: string;
  productId: string;
  quantity: number;
  checkoutPrice: number;
  product?: Product;
}
