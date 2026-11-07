import { Component, HostListener, inject, Output, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { ShoppingCartComponent } from "../../../shared/components/shopping-cart/shopping-cart.component";

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, ShoppingCartComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  private router = inject(Router)
  authStateService = inject(AuthStateService);
  user = this.authStateService.currentUser;
  isAuthenticated = this.authStateService.isAuthenticated;

  isVisibleMenu = signal(false);
  isVisibleShoppingCart = signal(false);

  logout() {
    this.authStateService.logout();
    this.router.navigateByUrl('/auth/login')
  }

  showMenu(event: Event) {
    event.stopPropagation()
    this.isVisibleMenu.update(p => !p)
  }

  @HostListener('document:click')
  closeMenu() {
    if (this.isVisibleMenu()) this.isVisibleMenu.set(false)
  }

  closeCart() {
    this.isVisibleShoppingCart.set(false)
  }
}
