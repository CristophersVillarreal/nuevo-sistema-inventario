import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Dashboard } from './layout/dashboard/dashboard';
import { Panel } from './pages/panel/panel';
import { Productos } from './pages/productos/productos';
import { Alertas } from './pages/alertas/alertas';
import { Asistente } from './pages/asistente/asistente';
import { authGuard } from './core/auth-guard';
import { Usuarios } from './pages/usuarios/usuarios';
import { Perfil } from './pages/perfil/perfil';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard],
    children: [
      { path: 'panel', component: Panel },
      { path: 'productos', component: Productos },
      { path: 'alertas', component: Alertas },
      { path: 'asistente', component: Asistente },
      { path: 'usuarios', component: Usuarios },
      { path: 'perfil', component: Perfil },
      { path: '', redirectTo: 'panel', pathMatch: 'full' },
    ],
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
];
