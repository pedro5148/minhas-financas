export interface Conta {
  id?: number;
  nome: string;
  saldoInicial: number;
  dataCriacao?: string;
  padrao?: boolean;
}

export interface ContaRequestDTO {
  nome: string;
  saldoInicial: number;
  padrao?: boolean;
}
