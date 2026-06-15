import { CartaoCredito } from './cartao-credito.model';

export enum StatusFatura {
  ABERTA = 'ABERTA',
  FECHADA = 'FECHADA',
  PAGA = 'PAGA'
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
