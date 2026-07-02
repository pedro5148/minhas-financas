export interface Categoria {
  id?: number;
  nome: string;
  permiteDetalhamento: boolean;
}

export interface CategoriaRequestDTO {
  nome: string;
  permiteDetalhamento?: boolean;
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
