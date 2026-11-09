import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Inventario, Usuario } from '../../core/inventario';

@Component({
  selector: 'app-usuarios',
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css',
})
export class Usuarios implements OnInit {
  usuarios = signal<Usuario[]>([]);
  cargando = signal(true);
  modalAbierto = signal(false);
  errorForm = signal('');

  username = '';
  password = '';
  rol = 'OPERADOR';
  nombre = '';
  apellidos = '';

  constructor(private inventario: Inventario) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.inventario.listarUsuarios().subscribe((data) => {
      this.usuarios.set(data);
      this.cargando.set(false);
    });
  }

  abrirNuevo(): void {
    this.username = '';
    this.password = '';
    this.rol = 'OPERADOR';
    this.nombre = '';
    this.apellidos = '';
    this.errorForm.set('');
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  guardar(): void {
    this.inventario
      .crearUsuario({
        username: this.username,
        password: this.password,
        rol: this.rol,
        nombre: this.nombre,
        apellidos: this.apellidos,
      })
      .subscribe({
        next: () => {
          this.modalAbierto.set(false);
          this.cargar();
        },
        error: (err) => {
          this.errorForm.set(err.error?.message || 'No se pudo crear el usuario.');
        },
      });
  }

  cambiarEstado(u: Usuario): void {
    const accion = u.activo ? 'desactivar' : 'activar';
    if (!confirm(`¿Seguro que quieres ${accion} a ${u.username}?`)) return;

    this.inventario.cambiarEstadoUsuario(u.id, !u.activo).subscribe(() => this.cargar());
  }
}
