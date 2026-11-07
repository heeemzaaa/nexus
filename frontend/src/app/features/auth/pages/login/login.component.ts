import { Component, inject } from '@angular/core';
import { AuthStateService } from '../../../../core/services/auth-state.service';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { StorageService } from '../../../../core/services/storage.service';

@Component({
  selector: 'app-login',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private authStateService = inject(AuthStateService);
  private router = inject(Router);

  loginForm = new FormGroup({
    email: new FormControl(""),
    password: new FormControl("")
  });

  onSubmit() {
    if (!this.email.value?.trim()) this.email.setErrors({ required: "User email cannot be empty" })
    if (!this.password.value?.trim()) this.password.setErrors({ required: "User password cannot be empty" })

    if (this.loginForm.invalid) return;

    this.authStateService.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: err => {
        // console.error(err)
        throw err
      }
    })
  }

  get email() {
    return this.loginForm.controls.email
  }

  get password() {
    return this.loginForm.controls.password
  }
}
