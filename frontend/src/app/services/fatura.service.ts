import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fatura } from '../models/fatura.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FaturaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/faturas`;

  buscarPorCartao(cartaoId: number): Observable<Fatura[]> {
    return this.http.get<Fatura[]>(`${this.apiUrl}/cartao/${cartaoId}`);
  }

  projetarProximasFaturas(cartaoId: number): Observable<Fatura[]> {
    return this.http.get<Fatura[]>(`${this.apiUrl}/cartao/${cartaoId}/proximas`);
  }

  pagarFatura(id: number): Observable<Fatura> {
    return this.http.post<Fatura>(`${this.apiUrl}/${id}/pagar`, {});
  }
}
