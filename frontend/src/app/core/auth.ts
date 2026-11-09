import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class Auth {
  private credenciales: string | null = null;
  autenticado = signal(false);
  usuario = signal('');
  rol = signal('');
  nombre = signal('');
  apellidos = signal('');
  foto = signal('');

  constructor(private http: HttpClient) {}

  async iniciarSesion(usuario: string, password: string): Promise<boolean> {
    const token = btoa(`${usuario}:${password}`);
    try {
      const yo = await firstValueFrom(
        this.http.get<{ usuario: string; rol: string; nombre: string; apellidos: string; foto: string }>(
          'http://localhost:8080/api/auth/me',
          { headers: { Authorization: `Basic ${token}` } }
        )
      );
      this.credenciales = token;
      this.autenticado.set(true);
      this.usuario.set(yo.usuario);
      this.rol.set(yo.rol);
      this.nombre.set(yo.nombre);
      this.apellidos.set(yo.apellidos);
      this.foto.set(yo.foto);
      return true;
    } catch {
      this.credenciales = null;
      this.autenticado.set(false);
      return false;
    }
  }

  actualizarDatosLocales(nombre: string, apellidos: string, foto: string): void {
    this.nombre.set(nombre);
    this.apellidos.set(apellidos);
    this.foto.set(foto);
  }

  cerrarSesion(): void {
    this.credenciales = null;
    this.autenticado.set(false);
    this.usuario.set('');
    this.rol.set('');
    this.nombre.set('');
    this.apellidos.set('');
    this.foto.set('');
  }

  esAdmin(): boolean {
    return this.rol() === 'ADMIN';
  }

  nombreMostrar(): string {
    return this.nombre() ? `${this.nombre()} ${this.apellidos()}`.trim() : this.usuario();
  }

  urlFoto(): string {
    return this.foto() ? `http://localhost:8080/fotos-perfil/${this.foto()}` : '';
  }

  obtenerToken(): string | null {
    return this.credenciales;
  }
}
