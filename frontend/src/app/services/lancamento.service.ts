import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { LancamentoResponseDTO, LancamentoRequestDTO } from '../models/lancamento.model';
import { Page } from '../models/page.model';
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

  listar(page: number = 0, size: number = 10, sort: string = 'dataLancamento,desc', descricao?: string, tipo?: string | null): Observable<Page<LancamentoResponseDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (descricao) {
      params = params.set('descricao', descricao);
    }
    
    if (tipo) {
      params = params.set('tipo', tipo);
    }

    return this.http.get<Page<LancamentoResponseDTO>>(this.apiUrl, { params }).pipe(
      map(pageObj => ({
        ...pageObj,
        content: this.parseValores(pageObj.content)
      }))
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

  importarEmLote(lancamentos: LancamentoRequestDTO[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/lote`, lancamentos);
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
