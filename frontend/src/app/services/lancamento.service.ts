import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Lancamento } from '../models/types';

@Injectable({
  providedIn: 'root'
})
export class LancamentoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/lancamentos';

  listarTodos(): Observable<Lancamento[]> {
    return this.http.get<Lancamento[]>(this.apiUrl);
  }

  listarPorMesAno(ano: number, mes: number): Observable<Lancamento[]> {
    return this.http.get<Lancamento[]>(`${this.apiUrl}/mes/${ano}/${mes}`);
  }

  listarPorFatura(faturaId: number): Observable<Lancamento[]> {
    return this.http.get<Lancamento[]>(`${this.apiUrl}/fatura/${faturaId}`);
  }

  criar(lancamento: Lancamento): Observable<Lancamento[]> {
    return this.http.post<Lancamento[]>(this.apiUrl, lancamento);
  }

  atualizar(id: number, lancamento: Lancamento): Observable<Lancamento> {
    return this.http.put<Lancamento>(`${this.apiUrl}/${id}`, lancamento);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
