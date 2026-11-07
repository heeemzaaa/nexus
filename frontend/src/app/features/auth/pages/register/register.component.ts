import { Component, inject } from '@angular/core';
import { Router, RouterLink } from "@angular/router";
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { AuthStateService } from '../../../../core/services/auth-state.service';
import { StorageService } from '../../../../core/services/storage.service';

@Component({
  selector: 'app-register',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private authStateService = inject(AuthStateService);
  private router = inject(Router);

  registerForm = new FormGroup({
    name: new FormControl(""),
    email: new FormControl(""),
    password: new FormControl(""),
    role: new FormControl("")
  });

  onSubmit() {
    if (!this.name.value?.trim()) this.name.setErrors({ required: "User name cannot be empty" })
    if (!this.email.value?.trim()) this.email.setErrors({ required: "User email cannot be empty" })
    if (!this.password.value) this.password.setErrors({ required: "User password cannot be empty" })
    if (!this.role.value?.trim()) this.role.setErrors({ required: "User role cannot be empty" })

    if (this.registerForm.invalid) return;

    this.authStateService.register(this.registerForm.value).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: err => {
        let fieldErrors = err.error?.validationErrors
        if (fieldErrors) {
          this.registerForm.setErrors(fieldErrors)
          return;
        }

        throw err
      }
    })
  }

  get name() {
    return this.registerForm.controls.name
  }

  get email() {
    return this.registerForm.controls.email
  }

  get password() {
    return this.registerForm.controls.password
  }

  get role() {
    return this.registerForm.controls.role
  }
}
