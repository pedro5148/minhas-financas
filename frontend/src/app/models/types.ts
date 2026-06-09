export enum TipoLancamento {
  DESPESA = 'DESPESA',
  RECEITA = 'RECEITA',
  TRANSFERENCIA = 'TRANSFERENCIA'
}

export enum StatusLancamento {
  PENDENTE = 'PENDENTE',
  EFETIVADO = 'EFETIVADO'
}

export enum TipoRecorrencia {
  NENHUMA = 'NENHUMA',
  MENSAL = 'MENSAL',
  PARCELADO = 'PARCELADO'
}

export enum StatusFatura {
  ABERTA = 'ABERTA',
  FECHADA = 'FECHADA',
  PAGA = 'PAGA'
}

export interface Conta {
  id?: number;
  nome: string;
  saldoInicial: number;
  dataCriacao?: string;
  padrao?: boolean;
}

export interface CartaoCredito {
  id?: number;
  nome: string;
  limiteTotal: number;
  diaFechamento: number;
  diaVencimento: number;
  contaPadrao: Conta;
  bandeira?: string;
  principal?: boolean;
}

export interface Fatura {
  id?: number;
  cartao: CartaoCredito;
  mesAno: string;
  valorTotal: number;
  valorPago: number;
  status: StatusFatura;
  dataVencimento: string;
  dataFechamento: string;
}

export interface Categoria {
  id?: number;
  nome: string;
}

export interface Subcategoria {
  id?: number;
  nome: string;
  categoria: Categoria;
}

export interface Lancamento {
  id?: number;
  tipo: TipoLancamento;
  descricao: string;
  valor: number;
  conta: Conta;
  contaDestino?: Conta;
  categoria?: Categoria;
  subcategoria?: Subcategoria;
  dataLancamento: string;
  dataVencimento: string;
  dataEfetivacao?: string;
  status: StatusLancamento;
  observacoes?: string;
  tipoRecorrencia: TipoRecorrencia;
  parcelaAtual?: number;
  totalParcelas?: number;
  cartaoCredito?: CartaoCredito;
  fatura?: Fatura;
}
