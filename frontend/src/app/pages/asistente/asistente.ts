import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AsistenteChat } from '../../core/asistente-chat';
import { MarkdownModule } from 'ngx-markdown';
@Component({
  selector: 'app-asistente',
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './asistente.html',
  styleUrl: './asistente.css',
})
export class Asistente {
  pregunta = '';
  enviando = signal(false);

  constructor(public chat: AsistenteChat, private http: HttpClient) {}

  enviar(): void {
    const texto = this.pregunta.trim();
    const id = this.chat.chatActivoId();
    if (!texto || this.enviando() || id === null) return;

    this.pregunta = '';
    this.enviando.set(true);
    this.chat.agregarMensajeUsuario(id, texto);

    this.http
      .post<{ respuesta: string }>('http://localhost:8080/api/asistente/consultar', { pregunta: texto })
      .subscribe({
        next: (res) => {
          this.chat.agregarRespuesta(id, res.respuesta);
          this.enviando.set(false);
        },
        error: () => {
          this.chat.agregarRespuesta(id, 'No se pudo conectar con el asistente. Intenta de nuevo.');
          this.enviando.set(false);
        },
      });
  }
}
