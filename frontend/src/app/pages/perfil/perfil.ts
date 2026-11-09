import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Inventario, PerfilUsuario } from '../../core/inventario';
import { Auth } from '../../core/auth';

@Component({
  selector: 'app-perfil',
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil implements OnInit {
  perfil = signal<PerfilUsuario | null>(null);
  cargando = signal(true);
  guardando = signal(false);
  mensaje = signal('');

  nombre = '';
  apellidos = '';

  constructor(private inventario: Inventario, private auth: Auth) {}

  ngOnInit(): void {
    this.inventario.verMiPerfil().subscribe((p) => {
      this.perfil.set(p);
      this.nombre = p.nombre;
      this.apellidos = p.apellidos;
      this.cargando.set(false);
    });
  }

  urlFoto(): string {
    const foto = this.perfil()?.foto;
    return foto ? `http://localhost:8080/fotos-perfil/${foto}` : '';
  }

  inicial(): string {
    return (this.perfil()?.username || '?').charAt(0).toUpperCase();
  }

  guardarDatos(): void {
    this.guardando.set(true);
    this.mensaje.set('');
    this.inventario.actualizarMiPerfil({ nombre: this.nombre, apellidos: this.apellidos }).subscribe({
      next: (p) => {
        this.perfil.set(p);
        this.guardando.set(false);
        this.mensaje.set('Datos actualizados correctamente.');
        this.auth.actualizarDatosLocales(p.nombre, p.apellidos, p.foto);
      },
      error: () => {
        this.guardando.set(false);
        this.mensaje.set('No se pudieron guardar los cambios.');
      },
    });
  }

  seleccionarFoto(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const archivo = input.files[0];

    this.inventario.subirFotoPerfil(archivo).subscribe({
      next: (p) => {
        this.perfil.set(p);
        this.mensaje.set('Foto actualizada correctamente.');
        this.auth.actualizarDatosLocales(p.nombre, p.apellidos, p.foto);
      },
      error: () => {
        this.mensaje.set('No se pudo subir la foto.');
      },
    });
  }
}
