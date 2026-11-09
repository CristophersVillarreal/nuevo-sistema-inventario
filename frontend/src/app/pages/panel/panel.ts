import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  NgApexchartsModule,
  ApexAxisChartSeries,
  ApexChart,
  ApexXAxis,
  ApexNonAxisChartSeries,
  ApexPlotOptions,
} from 'ng-apexcharts';
import { Inventario, Producto } from '../../core/inventario';

interface DiaCalendario {
  numero: number;
  fechaISO: string;
  tieneVencimiento: boolean;
  esHoy: boolean;
}

@Component({
  selector: 'app-panel',
  imports: [CommonModule, NgApexchartsModule],
  templateUrl: './panel.html',
  styleUrl: './panel.css',
})
export class Panel implements OnInit {
  total = signal(0);
  unidades = signal(0);
  valor = signal(0);
  bajos = signal<Producto[]>([]);
  cargando = signal(true);
  private productosCache: Producto[] = [];

  // --- Gráficos ---
  serieBarras: ApexAxisChartSeries = [];
  chartBarras: ApexChart = { type: 'bar', height: 280, toolbar: { show: false } };
  xaxisBarras: ApexXAxis = { categories: [] };

  serieLineas: ApexAxisChartSeries = [];
  chartLineas: ApexChart = { type: 'line', height: 280, toolbar: { show: false } };
  xaxisLineas: ApexXAxis = { categories: [] };
  tituloLineas = signal('Top 8 productos por stock');

  serieRadial: ApexNonAxisChartSeries = [0];
  chartRadial: ApexChart = { type: 'radialBar', height: 280 };
  plotOptionsRadial: ApexPlotOptions = {
    radialBar: {
      hollow: { size: '65%' },
      dataLabels: {
        value: { fontSize: '24px', fontWeight: 700, offsetY: 8, formatter: (v) => `${v}%` },
        name: { show: true, offsetY: -12, fontSize: '13px' },
      },
    },
  };

  // --- Calendario ---
  nombreMes = signal('');
  diasCalendario = signal<(DiaCalendario | null)[]>([]);
  fechaSeleccionada = signal<string | null>(null);
  productosDelDia = signal<Producto[]>([]);

  constructor(private inventario: Inventario) {}

  ngOnInit(): void {
    this.inventario.listarProductos().subscribe((productos) => {
      this.productosCache = productos;
      this.total.set(productos.length);
      this.unidades.set(productos.reduce((s, p) => s + p.stock, 0));
      this.valor.set(productos.reduce((s, p) => s + p.precio * p.stock, 0));
      this.bajos.set(productos.filter((p) => p.stock <= p.stockMinimo));
      this.cargando.set(false);

      this.armarGraficoBarras(productos);
      this.armarGraficoLineasGeneral(productos);
      this.armarGraficoRadial(productos);
      this.armarCalendario(productos);
    });
  }

  private armarGraficoBarras(productos: Producto[]): void {
    const porCategoria = new Map<string, number>();
    for (const p of productos) {
      const cat = p.categoria?.nombre || 'Sin categoría';
      porCategoria.set(cat, (porCategoria.get(cat) || 0) + p.stock);
    }
    this.xaxisBarras = { categories: [...porCategoria.keys()] };
    this.serieBarras = [{ name: 'Unidades en stock', data: [...porCategoria.values()] }];
  }

  private armarGraficoLineasGeneral(productos: Producto[]): void {
    const top = [...productos].sort((a, b) => b.stock - a.stock).slice(0, 8);
    this.tituloLineas.set('Top 8 productos por stock');
    this.xaxisLineas = { categories: top.map((p) => p.nombre) };
    this.serieLineas = [{ name: 'Stock actual', data: top.map((p) => p.stock) }];
  }

  private armarGraficoRadial(productos: Producto[]): void {
    const saludables = productos.filter((p) => p.stock > p.stockMinimo).length;
    const porcentaje = productos.length === 0 ? 100 : Math.round((saludables / productos.length) * 100);
    this.serieRadial = [porcentaje];
  }

  private armarCalendario(productos: Producto[]): void {
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = hoy.getMonth();

    this.nombreMes.set(
      hoy.toLocaleDateString('es-PE', { month: 'long', year: 'numeric' })
    );

    const fechasConVencimiento = new Set(
      productos.filter((p) => p.fechaVencimiento).map((p) => p.fechaVencimiento)
    );

    const primerDiaSemana = new Date(anio, mes, 1).getDay();
    const diasEnMes = new Date(anio, mes + 1, 0).getDate();
    const hoyISO = hoy.toISOString().slice(0, 10);

    const celdas: (DiaCalendario | null)[] = [];
    for (let i = 0; i < primerDiaSemana; i++) celdas.push(null);

    for (let dia = 1; dia <= diasEnMes; dia++) {
      const fechaISO = `${anio}-${String(mes + 1).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
      celdas.push({
        numero: dia,
        fechaISO,
        tieneVencimiento: fechasConVencimiento.has(fechaISO),
        esHoy: fechaISO === hoyISO,
      });
    }
    this.diasCalendario.set(celdas);
  }

  seleccionarDia(dia: DiaCalendario): void {
    this.fechaSeleccionada.set(dia.fechaISO);
    const delDia = this.productosCache.filter((p) => p.fechaVencimiento === dia.fechaISO);
    this.productosDelDia.set(delDia);

    if (delDia.length > 0) {
      this.tituloLineas.set(`Stock de productos que vencen el ${dia.fechaISO}`);
      this.xaxisLineas = { categories: delDia.map((p) => p.nombre) };
      this.serieLineas = [{ name: 'Stock actual', data: delDia.map((p) => p.stock) }];
    } else {
      this.armarGraficoLineasGeneral(this.productosCache);
    }
  }

  limpiarSeleccion(): void {
    this.fechaSeleccionada.set(null);
    this.productosDelDia.set([]);
    this.armarGraficoLineasGeneral(this.productosCache);
  }
}
