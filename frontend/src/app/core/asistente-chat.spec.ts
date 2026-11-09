import { TestBed } from '@angular/core/testing';

import { AsistenteChat } from './asistente-chat';

describe('AsistenteChat', () => {
  let service: AsistenteChat;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AsistenteChat);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
