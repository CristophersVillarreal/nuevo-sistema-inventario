import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Categoria {
  id: number;
  nombre: string;
}

export interface Producto {
  id: number;
  nombre: string;
  categoria: Categoria | null;
  ubicacion: string | null;
  fechaVencimiento: string | null;
  precio: number;
  stock: number;
  stockMinimo: number;
}

export interface Usuario {
  id: number;
  username: string;
  nombre: string;
  apellidos: string;
  rol: string;
  activo: boolean;
}

export interface NuevoUsuario {
  username: string;
  password: string;
  rol: string;
  nombre: string;
  apellidos: string;
}

export interface PerfilUsuario {
  username: string;
  nombre: string;
  apellidos: string;
  foto: string;
  rol: string;
}

const API = 'http://localhost:8080/api/productos';
const API_CATEGORIAS = 'http://localhost:8080/api/categorias';

@Injectable({ providedIn: 'root' })
export class Inventario {
  constructor(private http: HttpClient) {}

  listarProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(API);
  }

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(API_CATEGORIAS);
  }

  stockBajo(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${API}/stock-bajo`);
  }

  porVencer(dias = 30): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${API}/por-vencer?dias=${dias}`);
  }

  crear(producto: Partial<Producto>): Observable<Producto> {
    return this.http.post<Producto>(API, producto);
  }

  actualizar(id: number, producto: Partial<Producto>): Observable<Producto> {
    return this.http.put<Producto>(`${API}/${id}`, producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/${id}`);
  }

  listarUsuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>('http://localhost:8080/api/usuarios');
  }

  crearUsuario(usuario: NuevoUsuario): Observable<Usuario> {
    return this.http.post<Usuario>('http://localhost:8080/api/usuarios', usuario);
  }

  cambiarEstadoUsuario(id: number, activo: boolean): Observable<Usuario> {
    return this.http.put<Usuario>(`http://localhost:8080/api/usuarios/${id}/estado`, { activo });
  }
  verMiPerfil(): Observable<PerfilUsuario> {
    return this.http.get<PerfilUsuario>('http://localhost:8080/api/perfil');
  }

  actualizarMiPerfil(datos: { nombre: string; apellidos: string }): Observable<PerfilUsuario> {
    return this.http.put<PerfilUsuario>('http://localhost:8080/api/perfil', datos);
  }

  subirFotoPerfil(archivo: File): Observable<PerfilUsuario> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<PerfilUsuario>('http://localhost:8080/api/perfil/foto', formData);
  }
}
