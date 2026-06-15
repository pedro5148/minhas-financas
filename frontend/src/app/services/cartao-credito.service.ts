import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartaoCredito, CartaoCreditoRequestDTO } from '../models/cartao-credito.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CartaoCreditoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/cartoes`;

  listarTodos(): Observable<CartaoCredito[]> {
    return this.http.get<CartaoCredito[]>(this.apiUrl);
  }

  criar(cartao: CartaoCreditoRequestDTO): Observable<CartaoCredito> {
    return this.http.post<CartaoCredito>(this.apiUrl, cartao);
  }

  atualizar(id: number, cartao: CartaoCreditoRequestDTO): Observable<CartaoCredito> {
    return this.http.put<CartaoCredito>(`${this.apiUrl}/${id}`, cartao);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
