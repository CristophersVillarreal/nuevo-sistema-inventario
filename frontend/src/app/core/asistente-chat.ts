import { Injectable, signal } from '@angular/core';

export interface Mensaje {
  texto: string;
  quien: 'usuario' | 'bot';
}

export interface Chat {
  id: number;
  titulo: string;
  mensajes: Mensaje[];
}

@Injectable({ providedIn: 'root' })
export class AsistenteChat {
  chats = signal<Chat[]>([]);
  chatActivoId = signal<number | null>(null);
  private contador = 0;

  constructor() {
    this.nuevoChat();
  }

  chatActivo(): Chat | undefined {
    return this.chats().find((c) => c.id === this.chatActivoId());
  }

  nuevoChat(): void {
    const chat: Chat = { id: ++this.contador, titulo: 'Nueva consulta', mensajes: [] };
    this.chats.update((lista) => [chat, ...lista]);
    this.chatActivoId.set(chat.id);
  }

  seleccionarChat(id: number): void {
    this.chatActivoId.set(id);
  }

  agregarMensajeUsuario(idChat: number, texto: string): void {
    this.chats.update((lista) =>
      lista.map((c) => {
        if (c.id !== idChat) return c;
        const esPrimerMensaje = c.mensajes.length === 0;
        return {
          ...c,
          titulo: esPrimerMensaje ? texto.slice(0, 28) : c.titulo,
          mensajes: [...c.mensajes, { texto, quien: 'usuario' as const }],
        };
      })
    );
  }

  agregarRespuesta(idChat: number, texto: string): void {
    this.chats.update((lista) =>
      lista.map((c) => (c.id === idChat ? { ...c, mensajes: [...c.mensajes, { texto, quien: 'bot' as const }] } : c))
    );
  }
}
