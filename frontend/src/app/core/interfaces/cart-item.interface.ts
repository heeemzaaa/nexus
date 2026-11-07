import { Product } from "./product.interface";

export interface CartItem {
    id: string,
    shopping_cart_id: string,
    product_id: string,
    item_quantity: number,
    product: Product
}