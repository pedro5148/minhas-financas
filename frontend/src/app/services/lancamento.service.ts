import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { map, shareReplay, tap } from 'rxjs/operators';
import { LancamentoResponseDTO, LancamentoRequestDTO, NfceParseRequestDTO, NfceEfetivarRequestDTO } from '../models/lancamento.model';
import { Page } from '../models/page.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LancamentoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/lancamentos`;

  private lancamentosCache$?: Observable<LancamentoResponseDTO[]>;
  private cacheAno?: number;
  private cacheMes?: number;

  private lancamentoAlteradoSource = new Subject<void>();
  lancamentoAlterado$ = this.lancamentoAlteradoSource.asObservable();

  notificarAlteracao() {
    this.limparCacheDashboard();
    this.lancamentoAlteradoSource.next();
  }

  limparCacheDashboard() {
    this.lancamentosCache$ = undefined;
    this.cacheAno = undefined;
    this.cacheMes = undefined;
  }

  private parseValores(lancamentos: LancamentoResponseDTO[]): LancamentoResponseDTO[] {
    return lancamentos.map(l => ({
      ...l,
      valor: typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor
    }));
  }

  listar(page: number = 0, size: number = 10, sort: string = 'dataLancamento,desc', descricao?: string, tipo?: string | null, mes?: number | null, ano?: number | null): Observable<Page<LancamentoResponseDTO>> {
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

    if (mes !== undefined && mes !== null) {
      params = params.set('mes', mes.toString());
    }

    if (ano !== undefined && ano !== null) {
      params = params.set('ano', ano.toString());
    }

    return this.http.get<Page<LancamentoResponseDTO>>(this.apiUrl, { params }).pipe(
      map(pageObj => ({
        ...pageObj,
        content: this.parseValores(pageObj.content)
      }))
    );
  }

  listarPorMesAno(ano: number, mes: number): Observable<LancamentoResponseDTO[]> {
    if (this.lancamentosCache$ && this.cacheAno === ano && this.cacheMes === mes) {
      return this.lancamentosCache$;
    }

    this.cacheAno = ano;
    this.cacheMes = mes;
    this.lancamentosCache$ = this.http.get<LancamentoResponseDTO[]>(`${this.apiUrl}/mes/${ano}/${mes}`).pipe(
      map(res => this.parseValores(res)),
      shareReplay(1)
    );

    return this.lancamentosCache$;
  }

  listarPorFatura(faturaId: number): Observable<LancamentoResponseDTO[]> {
    return this.http.get<LancamentoResponseDTO[]>(`${this.apiUrl}/fatura/${faturaId}`).pipe(
      map(res => this.parseValores(res))
    );
  }

  criar(lancamento: LancamentoRequestDTO): Observable<LancamentoResponseDTO[]> {
    return this.http.post<LancamentoResponseDTO[]>(this.apiUrl, lancamento).pipe(
      tap(() => this.limparCacheDashboard())
    );
  }

  importarEmLote(lancamentos: LancamentoRequestDTO[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/lote`, lancamentos).pipe(
      tap(() => this.limparCacheDashboard())
    );
  }

  atualizar(id: number, lancamento: LancamentoRequestDTO): Observable<LancamentoResponseDTO> {
    return this.http.put<LancamentoResponseDTO>(`${this.apiUrl}/${id}`, lancamento).pipe(
      map(l => ({
        ...l,
        valor: typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor
      })),
      tap(() => this.limparCacheDashboard())
    );
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.limparCacheDashboard())
    );
  }

  previewNfce(payload: NfceParseRequestDTO): Observable<LancamentoResponseDTO> {
    return this.http.post<LancamentoResponseDTO>(`${this.apiUrl}/nfce/preview`, payload).pipe(
      map(l => ({
        ...l,
        valor: typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor
      }))
    );
  }

  efetivarNfce(payload: NfceEfetivarRequestDTO): Observable<LancamentoResponseDTO> {
    return this.http.post<LancamentoResponseDTO>(`${this.apiUrl}/nfce/efetivar`, payload).pipe(
      tap(() => this.limparCacheDashboard()),
      map(l => ({
        ...l,
        valor: typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor
      }))
    );
  }
}
