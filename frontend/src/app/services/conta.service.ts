import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay, tap } from 'rxjs/operators';
import { Conta, ContaRequestDTO } from '../models/conta.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ContaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/contas`;

  private contasCache$?: Observable<Conta[]>;

  listarTodos(): Observable<Conta[]> {
    if (!this.contasCache$) {
      this.contasCache$ = this.http.get<Conta[]>(this.apiUrl).pipe(
        shareReplay(1)
      );
    }
    return this.contasCache$;
  }

  limparCache() {
    this.contasCache$ = undefined;
  }

  criar(conta: ContaRequestDTO): Observable<Conta> {
    return this.http.post<Conta>(this.apiUrl, conta).pipe(
      tap(() => this.limparCache())
    );
  }

  atualizar(id: number, conta: ContaRequestDTO): Observable<Conta> {
    return this.http.put<Conta>(`${this.apiUrl}/${id}`, conta).pipe(
      tap(() => this.limparCache())
    );
  }
}
