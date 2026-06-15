import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Conta, ContaRequestDTO } from '../models/conta.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ContaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/contas`;

  listarTodos(): Observable<Conta[]> {
    return this.http.get<Conta[]>(this.apiUrl);
  }

  criar(conta: ContaRequestDTO): Observable<Conta> {
    return this.http.post<Conta>(this.apiUrl, conta);
  }

  atualizar(id: number, conta: ContaRequestDTO): Observable<Conta> {
    return this.http.put<Conta>(`${this.apiUrl}/${id}`, conta);
  }
}
