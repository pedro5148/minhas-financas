import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria, Subcategoria, CategoriaRequestDTO, SubcategoriaRequestDTO } from '../models/categoria.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}`;

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/categorias`);
  }

  atualizarCategoria(id: number, categoriaRequest: CategoriaRequestDTO): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/categorias/${id}`, categoriaRequest);
  }

  criarCategoria(categoriaRequest: CategoriaRequestDTO): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.apiUrl}/categorias`, categoriaRequest);
  }

  excluirCategoria(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/categorias/${id}`, { responseType: 'text' });
  }

  listarSubcategorias(): Observable<Subcategoria[]> {
    return this.http.get<Subcategoria[]>(`${this.apiUrl}/subcategorias`);
  }

  listarSubcategoriasPorCategoria(categoriaId: number): Observable<Subcategoria[]> {
    return this.http.get<Subcategoria[]>(`${this.apiUrl}/subcategorias/categoria/${categoriaId}`);
  }

  criarSubcategoria(categoriaId: number, nome: string): Observable<Subcategoria> {
    const request: SubcategoriaRequestDTO = { categoriaId, nome };
    return this.http.post<Subcategoria>(`${this.apiUrl}/subcategorias`, request);
  }

  excluirSubcategoria(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/subcategorias/${id}`, { responseType: 'text' });
  }
}
