import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartaoCredito, CartaoCreditoRequestDTO } from '../models/cartao-credito.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CartaoCreditoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/cartoes`;

  listarTodos(mes?: number, ano?: number): Observable<CartaoCredito[]> {
    let params = new HttpParams();
    if (mes !== undefined && mes !== null) {
      params = params.set('mes', mes.toString());
    }
    if (ano !== undefined && ano !== null) {
      params = params.set('ano', ano.toString());
    }
    return this.http.get<CartaoCredito[]>(this.apiUrl, { params });
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
