export interface Order {
  id: string;
  userId: string;
  total: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}
