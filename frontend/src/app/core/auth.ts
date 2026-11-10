import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

// Respuesta de POST /api/auth/login
interface RespuestaLogin {
  token: string;
  tipo: string;
  expiraEnSegundos: number;
  usuario: string;
  rol: string;
  nombre: string;
  apellidos: string;
  foto: string;
}

// Lo que se guarda en sessionStorage: sobrevive a recargar la pagina (F5),
// pero se borra al cerrar la pestana.
interface SesionGuardada {
  token: string;
  usuario: string;
  rol: string;
  nombre: string;
  apellidos: string;
  foto: string;
}

const CLAVE_SESION = 'laesquina.sesion';

@Injectable({ providedIn: 'root' })
export class Auth {
  private token: string | null = null;
  autenticado = signal(false);
  usuario = signal('');
  rol = signal('');
  nombre = signal('');
  apellidos = signal('');
  foto = signal('');

  constructor(private http: HttpClient) {
    this.restaurarSesion();
  }

  async iniciarSesion(usuario: string, password: string): Promise<boolean> {
    try {
      const r = await firstValueFrom(
        this.http.post<RespuestaLogin>('http://localhost:8080/api/auth/login', { username: usuario, password })
      );
      this.aplicarSesion({
        token: r.token,
        usuario: r.usuario,
        rol: r.rol,
        nombre: r.nombre,
        apellidos: r.apellidos,
        foto: r.foto,
      });
      return true;
    } catch {
      this.cerrarSesion();
      return false;
    }
  }

  actualizarDatosLocales(nombre: string, apellidos: string, foto: string): void {
    this.nombre.set(nombre);
    this.apellidos.set(apellidos);
    this.foto.set(foto);
    this.guardar();
  }

  cerrarSesion(): void {
    this.token = null;
    this.autenticado.set(false);
    this.usuario.set('');
    this.rol.set('');
    this.nombre.set('');
    this.apellidos.set('');
    this.foto.set('');
    sessionStorage.removeItem(CLAVE_SESION);
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
    // Si el token ya expiro, la sesion se cierra y el interceptor no lo envia.
    if (this.token && this.tokenExpirado(this.token)) {
      this.cerrarSesion();
    }
    return this.token;
  }

  private aplicarSesion(s: SesionGuardada): void {
    this.token = s.token;
    this.usuario.set(s.usuario);
    this.rol.set(s.rol);
    this.nombre.set(s.nombre);
    this.apellidos.set(s.apellidos);
    this.foto.set(s.foto);
    this.autenticado.set(true);
    this.guardar();
  }

  private guardar(): void {
    if (!this.token) return;
    const s: SesionGuardada = {
      token: this.token,
      usuario: this.usuario(),
      rol: this.rol(),
      nombre: this.nombre(),
      apellidos: this.apellidos(),
      foto: this.foto(),
    };
    sessionStorage.setItem(CLAVE_SESION, JSON.stringify(s));
  }

  private restaurarSesion(): void {
    const texto = sessionStorage.getItem(CLAVE_SESION);
    if (!texto) return;
    try {
      const s = JSON.parse(texto) as SesionGuardada;
      if (s.token && !this.tokenExpirado(s.token)) {
        this.aplicarSesion(s);
      } else {
        sessionStorage.removeItem(CLAVE_SESION);
      }
    } catch {
      sessionStorage.removeItem(CLAVE_SESION);
    }
  }

  // Lee la fecha de expiracion (exp) del token. Solo sirve para no enviar tokens vencidos:
  // quien realmente valida la firma y la expiracion es el backend.
  private tokenExpirado(token: string): boolean {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const datos = JSON.parse(atob(base64));
      return typeof datos.exp !== 'number' || datos.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }
}
