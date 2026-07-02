import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProdutoResponseDTO } from '../models/lancamento.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/produtos`;

  search(termo: string): Observable<ProdutoResponseDTO[]> {
    if (!termo.trim()) return of([]);
    let params = new HttpParams().set('nome', termo);
    return this.http.get<ProdutoResponseDTO[]>(`${this.apiUrl}/search`, { params }).pipe(
      catchError(() => of([]))
    );
  }
}
