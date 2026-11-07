import { User } from "./user.interface"

export interface Product {
    id: string,
    name: string,
    price: number,
    description: string,
    quantity: number,
    status?: string,
    user_id: string,
    user_infos?: User,
    files?: string[]
}
