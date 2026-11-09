import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownModule } from 'ngx-markdown';
import { Auth } from '../../core/auth';
import { Inventario, Producto, Categoria } from '../../core/inventario';

@Component({
  selector: 'app-productos',
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './productos.html',
  styleUrl: './productos.css',
})
export class Productos implements OnInit {
  productos = signal<Producto[]>([]);
  categorias = signal<Categoria[]>([]);
  cargando = signal(true);

  modalAbierto = signal(false);
  modalTitulo = signal('Nuevo producto');
  errorForm = signal('');

  // Campos del formulario
  editandoId: number | null = null;
  nombre = '';
  categoriaId: number | null = null;
  ubicacion = '';
  fechaVencimiento = '';
  precio = 0;
  stock = 0;
  stockMinimo = 0;

  constructor(private inventario: Inventario, public auth: Auth) {}
  ngOnInit(): void {
    this.cargarTodo();
  }

  cargarTodo(): void {
    this.cargando.set(true);
    this.inventario.listarProductos().subscribe((productos) => {
      this.productos.set(productos);
      this.cargando.set(false);
    });
    this.inventario.listarCategorias().subscribe((categorias) => {
      this.categorias.set(categorias);
    });
  }

  abrirNuevo(): void {
    this.editandoId = null;
    this.nombre = '';
    this.categoriaId = null;
    this.ubicacion = '';
    this.fechaVencimiento = '';
    this.precio = 0;
    this.stock = 0;
    this.stockMinimo = 0;
    this.errorForm.set('');
    this.modalTitulo.set('Nuevo producto');
    this.modalAbierto.set(true);
  }

  editar(p: Producto): void {
    this.editandoId = p.id;
    this.nombre = p.nombre;
    this.categoriaId = p.categoria?.id ?? null;
    this.ubicacion = p.ubicacion ?? '';
    this.fechaVencimiento = p.fechaVencimiento ?? '';
    this.precio = p.precio;
    this.stock = p.stock;
    this.stockMinimo = p.stockMinimo;
    this.errorForm.set('');
    this.modalTitulo.set('Editar producto');
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
  }

  guardar(): void {
    const producto: Partial<Producto> = {
      nombre: this.nombre,
      categoria: this.categoriaId ? { id: this.categoriaId, nombre: '' } : null,
      ubicacion: this.ubicacion || null,
      fechaVencimiento: this.fechaVencimiento || null,
      precio: this.precio,
      stock: this.stock,
      stockMinimo: this.stockMinimo,
    };

    const peticion = this.editandoId
      ? this.inventario.actualizar(this.editandoId, producto)
      : this.inventario.crear(producto);

    peticion.subscribe({
      next: () => {
        this.modalAbierto.set(false);
        this.cargarTodo();
      },
      error: (err) => {
        this.errorForm.set(err.error?.mensaje || 'No se pudo guardar el producto.');
      },
    });
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este producto?')) return;
    this.inventario.eliminar(id).subscribe({
      next: () => this.cargarTodo(),
      error: (err) => {
        alert('No se pudo eliminar: ' + (err.error?.mensaje || err.message));
      },
    });
  }

  stockBajo(p: Producto): boolean {
    return p.stock <= p.stockMinimo;
  }
}
