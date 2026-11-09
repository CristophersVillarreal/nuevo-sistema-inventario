import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../core/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  usuario = '';
  password = '';
  cargando = false;
  error = '';

  constructor(private auth: Auth, private router: Router) {}

  async ingresar(): Promise<void> {
    this.error = '';
    this.cargando = true;
    const ok = await this.auth.iniciarSesion(this.usuario, this.password);
    this.cargando = false;

    if (ok) {
      this.router.navigate(['/dashboard'])
    } else {
      this.error = 'Usuario o contraseña incorrectos.';
    }
  }
}
