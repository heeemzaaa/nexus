import { Routes } from '@angular/router';
import { FeedComponent } from './feed/feed.component';
import { authGuard } from '../../core/guards/auth.guard';
import { NotfoundErrorComponent } from '../../shared/components/notfound-error/notfound-error.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'feed',
  },
  {
    path: 'profile/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('../profile/profile.component').then((m) => m.ProfileComponent),
  },
  {
    path: 'feed',
    component: FeedComponent,
  },
  {
    path: '**',
    component: NotfoundErrorComponent,
  },
];
