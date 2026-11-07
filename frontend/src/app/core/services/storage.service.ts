import { Injectable } from '@angular/core';
// import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class StorageService {
  setToken(token: string): void {
    localStorage.setItem("jwt", token);
  }

  getToken(): string | null {
    return localStorage.getItem("jwt");
  }

  clearAuth(): void {
    localStorage.removeItem("jwt");
  }

  clear(): void {
    localStorage.clear();
  }
}
