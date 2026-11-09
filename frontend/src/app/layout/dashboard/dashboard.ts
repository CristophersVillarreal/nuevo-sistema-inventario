import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { Auth } from '../../core/auth';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  colapsado = signal(false);

  constructor(public auth: Auth, private router: Router) {}

  alternarSidebar(): void {
    this.colapsado.update((v) => !v);
  }

  inicial(): string {
    return (this.auth.usuario() || '?').charAt(0).toUpperCase();
  }

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }
}
