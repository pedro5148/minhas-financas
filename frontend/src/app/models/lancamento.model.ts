import { Conta } from './conta.model';
import { Categoria, Subcategoria } from './categoria.model';
import { CartaoCredito } from './cartao-credito.model';
import { Fatura } from './fatura.model';

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

export interface LancamentoResponseDTO {
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

export interface LancamentoRequestDTO {
  tipo: TipoLancamento;
  descricao: string;
  valor: number;
  contaId: number;
  contaDestinoId?: number | null;
  categoriaId?: number | null;
  subcategoriaId?: number | null;
  dataLancamento: string;
  dataVencimento: string;
  dataEfetivacao?: string | null;
  status: StatusLancamento;
  observacoes?: string;
  tipoRecorrencia: TipoRecorrencia;
  totalParcelas?: number | null;
  cartaoCreditoId?: number | null;
}
