import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categoria, Subcategoria } from '../models/types';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api';

  listarCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.apiUrl}/categorias`);
  }

  atualizarCategoria(id: number, categoriaRequest: any): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.apiUrl}/categorias/${id}`, categoriaRequest);
  }

  criarCategoria(categoriaRequest: any): Observable<Categoria> {
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
    return this.http.post<Subcategoria>(`${this.apiUrl}/subcategorias`, { categoriaId, nome });
  }

  excluirSubcategoria(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/subcategorias/${id}`, { responseType: 'text' });
  }
}
