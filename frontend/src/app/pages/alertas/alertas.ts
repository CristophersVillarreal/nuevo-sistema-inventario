import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Inventario, Producto } from '../../core/inventario';

@Component({
  selector: 'app-alertas',
  imports: [CommonModule],
  templateUrl: './alertas.html',
  styleUrl: './alertas.css',
})
export class Alertas implements OnInit {
  bajos = signal<Producto[]>([]);
  porVencer = signal<Producto[]>([]);
  cargando = signal(true);

  constructor(private inventario: Inventario) {}

  ngOnInit(): void {
    this.inventario.stockBajo().subscribe((data) => {
      this.bajos.set(data);
      this.cargando.set(false);
    });
    this.inventario.porVencer(30).subscribe((data) => {
      this.porVencer.set(data);
    });
  }

  severidad(p: Producto): 'critico' | 'alerta' {
    return p.stock <= p.stockMinimo / 2 ? 'critico' : 'alerta';
  }

  porcentajeStock(p: Producto): number {
    if (p.stockMinimo === 0) return 100;
    return Math.min(100, Math.round((p.stock / p.stockMinimo) * 100));
  }

  diasParaVencer(fecha: string | null): number {
    if (!fecha) return 999;
    const hoy = new Date();
    const venc = new Date(fecha);
    return Math.ceil((venc.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
  }

  urgenciaVencimiento(dias: number): 'critico' | 'alerta' | 'ok' {
    if (dias <= 7) return 'critico';
    if (dias <= 15) return 'alerta';
    return 'ok';
  }
}
