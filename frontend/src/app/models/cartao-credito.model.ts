import { Conta } from './conta.model';

export interface CartaoCredito {
  id?: number;
  nome: string;
  limiteTotal: number;
  diaFechamento: number;
  diaVencimento: number;
  contaPadrao: Conta;
  bandeira?: string;
  principal?: boolean;
  valorFatura?: number;
}

export interface CartaoCreditoRequestDTO {
  nome: string;
  limiteTotal: number;
  diaFechamento: number;
  diaVencimento: number;
  contaPadraoId: number;
  bandeira?: string;
  principal?: boolean;
}
