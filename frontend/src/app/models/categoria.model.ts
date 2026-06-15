export interface Categoria {
  id?: number;
  nome: string;
}

export interface CategoriaRequestDTO {
  nome: string;
}

export interface Subcategoria {
  id?: number;
  nome: string;
  categoria: Categoria;
}

export interface SubcategoriaRequestDTO {
  categoriaId: number;
  nome: string;
}
