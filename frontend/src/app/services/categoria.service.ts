import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay, tap } from 'rxjs/operators';
import { Categoria, Subcategoria, CategoriaRequestDTO, SubcategoriaRequestDTO } from '../models/categoria.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}`;

  private categoriasCache$?: Observable<Categoria[]>;
  private subcategoriasCache$?: Observable<Subcategoria[]>;

  limparCache() {
    this.categoriasCache$ = undefined;
    this.subcategoriasCache$ = undefined;
  }

  listarCategorias(): Observable<Categoria[]> {
    if (!this.categoriasCache$) {
      this.categoriasCache$ = this.http.get<Categoria[]>(`${this.apiUrl}/categorias`).pipe(
        shareReplay(1)
      );
    }
    return this.categoriasCache$;
  }

  atualizarCategoria(id: number, categoriaRequest: CategoriaRequestDTO): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/categorias/${id}`, categoriaRequest).pipe(
      tap(() => this.limparCache())
    );
  }

  criarCategoria(categoriaRequest: CategoriaRequestDTO): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.apiUrl}/categorias`, categoriaRequest).pipe(
      tap(() => this.limparCache())
    );
  }

  excluirCategoria(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/categorias/${id}`, { responseType: 'text' }).pipe(
      tap(() => this.limparCache())
    );
  }

  listarSubcategorias(): Observable<Subcategoria[]> {
    if (!this.subcategoriasCache$) {
      this.subcategoriasCache$ = this.http.get<Subcategoria[]>(`${this.apiUrl}/subcategorias`).pipe(
        shareReplay(1)
      );
    }
    return this.subcategoriasCache$;
  }

  listarSubcategoriasPorCategoria(categoriaId: number): Observable<Subcategoria[]> {
    return this.http.get<Subcategoria[]>(`${this.apiUrl}/subcategorias/categoria/${categoriaId}`);
  }

  criarSubcategoria(categoriaId: number, nome: string): Observable<Subcategoria> {
    const request: SubcategoriaRequestDTO = { categoriaId, nome };
    return this.http.post<Subcategoria>(`${this.apiUrl}/subcategorias`, request).pipe(
      tap(() => this.limparCache())
    );
  }

  excluirSubcategoria(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/subcategorias/${id}`, { responseType: 'text' }).pipe(
      tap(() => this.limparCache())
    );
  }
}
