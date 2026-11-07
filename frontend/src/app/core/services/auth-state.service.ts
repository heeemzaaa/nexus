import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal, computed } from '@angular/core';
import { API } from '../config/api';
import { User } from '../interfaces/user.interface';
import { catchError, of, tap } from 'rxjs';
import { StorageService } from './storage.service';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthStateService {
  private http = inject(HttpClient)
  private storage = inject(StorageService)

  private _currentUser = signal<User | null>(null);
  readonly currentUser = computed(() => this._currentUser());
  readonly isAuthenticated = computed(() => !!this._currentUser());

  setCurrentUser(token: string) {
    try {
      const decoded: any = jwtDecode(token);
      this._currentUser.set({
        id: decoded.id,
        name: decoded.name,
        email: decoded.sub,
        role: decoded.role,
        avatar: decoded.avatar || `https://ui-avatars.com/api/?name=${decoded.name}&background=random`,
        token: token
      });
    } catch (e) {
      console.error('Error decoding token', e);
      this.logout();
    }
  }

  fetchCurrentUser() {
    return this.http.get<{ user_details: User }>(API.ME).pipe(
      tap(respone => {
        this._currentUser.set(respone.user_details)
      }),
      catchError(() => {
        this._currentUser.set(null);
        return of(null);
      })
    );
  }

  register(userData: any) {
    return this.http.post<{ token: string }>(API.REGISTER, userData).pipe(
      tap(res => {
        this.storage.setToken(res.token);
        this.setCurrentUser(res.token);
      })
    )
  }

  login(userData: any) {
    return this.http.post<{ token: string }>(API.LOGIN, userData).pipe(
      tap(res => {
        this.storage.setToken(res.token);
        this.setCurrentUser(res.token);
      })
    )
  }

  logout() {
    this.storage.clearAuth();
    this._currentUser.set(null);
  }

  fetchUserProfile(id: string) {
    return this.http.get<{ user_details: User }>(`${API.PROFILE}/${id}`)
  }
}
