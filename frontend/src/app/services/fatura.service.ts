import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fatura } from '../models/types';

@Injectable({
  providedIn: 'root'
})
export class FaturaService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/faturas';

  buscarPorCartao(cartaoId: number): Observable<Fatura[]> {
    return this.http.get<Fatura[]>(`${this.apiUrl}/cartao/${cartaoId}`);
  }

  pagarFatura(id: number): Observable<Fatura> {
    return this.http.post<Fatura>(`${this.apiUrl}/${id}/pagar`, {});
  }
}
