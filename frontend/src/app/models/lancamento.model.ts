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

export interface EstabelecimentoResponseDTO {
  id?: number;
  nome: string;
  cnpj: string;
}

export interface ProdutoResponseDTO {
  id?: number;
  nome: string;
  codigo?: string;
  unidade?: string;
}

export interface ItemLancamentoResponseDTO {
  id?: number;
  produto: ProdutoResponseDTO;
  quantidade: number;
  valorUnitarioBruto: number;
  valorTotalBruto: number;
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
  // Propriedades vindas do preview da NFC-e
  valorBruto?: number;
  valorDesconto?: number;
  chaveNfce?: string;
  estabelecimento?: EstabelecimentoResponseDTO;
  itens?: ItemLancamentoResponseDTO[];
}

export interface LancamentoRequestDTO {
  tipo: TipoLancamento;
  descricao: string;
  valor: number;
  contaId: number;
  contaDestinoId?: number | null;
  categoriaId: number;
  subcategoriaId?: number | null;
  dataLancamento: string;
  dataVencimento: string;
  dataEfetivacao?: string | null;
  status: StatusLancamento;
  observacoes?: string;
  tipoRecorrencia: TipoRecorrencia;
  totalParcelas?: number | null;
  cartaoCreditoId?: number | null;
  faturaId?: number | null;
  mesFatura?: number | null;
  anoFatura?: number | null;
}

export interface NfceParseRequestDTO {
  contaId: number;
  categoriaId: number;
  subcategoriaId?: number | null;
  dataPagamento?: string | null;
  htmlContent: string;
}

export interface NfceEfetivarRequestDTO extends LancamentoRequestDTO {
  valorBruto: number;
  valorDesconto: number;
  chaveNfce?: string;
  estabelecimento?: EstabelecimentoResponseDTO;
  itens?: ItemLancamentoResponseDTO[];
}
