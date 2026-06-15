import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { LancamentoResponseDTO, LancamentoRequestDTO } from '../models/lancamento.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LancamentoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/lancamentos`;

  private lancamentoAlteradoSource = new Subject<void>();
  lancamentoAlterado$ = this.lancamentoAlteradoSource.asObservable();

  notificarAlteracao() {
    this.lancamentoAlteradoSource.next();
  }

  private parseValores(lancamentos: LancamentoResponseDTO[]): LancamentoResponseDTO[] {
    return lancamentos.map(l => ({
      ...l,
      valor: typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor
    }));
  }

  listarTodos(): Observable<LancamentoResponseDTO[]> {
    return this.http.get<LancamentoResponseDTO[]>(this.apiUrl).pipe(
      map(res => this.parseValores(res))
    );
  }

  listarPorMesAno(ano: number, mes: number): Observable<LancamentoResponseDTO[]> {
    return this.http.get<LancamentoResponseDTO[]>(`${this.apiUrl}/mes/${ano}/${mes}`).pipe(
      map(res => this.parseValores(res))
    );
  }

  listarPorFatura(faturaId: number): Observable<LancamentoResponseDTO[]> {
    return this.http.get<LancamentoResponseDTO[]>(`${this.apiUrl}/fatura/${faturaId}`).pipe(
      map(res => this.parseValores(res))
    );
  }

  criar(lancamento: LancamentoRequestDTO): Observable<LancamentoResponseDTO[]> {
    return this.http.post<LancamentoResponseDTO[]>(this.apiUrl, lancamento);
  }

  atualizar(id: number, lancamento: LancamentoRequestDTO): Observable<LancamentoResponseDTO> {
    return this.http.put<LancamentoResponseDTO>(`${this.apiUrl}/${id}`, lancamento).pipe(
      map(l => ({
        ...l,
        valor: typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor
      }))
    );
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
