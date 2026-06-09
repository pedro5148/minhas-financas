import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartaoCredito } from '../models/types';

@Injectable({
  providedIn: 'root'
})
export class CartaoCreditoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/cartoes';

  listarTodos(): Observable<CartaoCredito[]> {
    return this.http.get<CartaoCredito[]>(this.apiUrl);
  }

  criar(cartao: CartaoCredito): Observable<CartaoCredito> {
    return this.http.post<CartaoCredito>(this.apiUrl, cartao);
  }

  atualizar(id: number, cartao: CartaoCredito): Observable<CartaoCredito> {
    return this.http.put<CartaoCredito>(`${this.apiUrl}/${id}`, cartao);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
