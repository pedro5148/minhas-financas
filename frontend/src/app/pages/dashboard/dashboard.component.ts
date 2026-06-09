import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LancamentoService } from '../../services/lancamento.service';
import { ContaService } from '../../services/conta.service';
import { Lancamento, TipoLancamento, StatusLancamento } from '../../models/types';
import { LancamentoModalComponent } from '../../components/lancamento-modal/lancamento-modal.component';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatTooltipModule, MatDialogModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private lancamentoService = inject(LancamentoService);
  private contaService = inject(ContaService);
  private dialog = inject(MatDialog);

  mesAtual: Date = new Date();

  lancamentos: Lancamento[] = [];
  ultimasDespesas: Lancamento[] = [];
  ultimasReceitas: Lancamento[] = [];

  saldoInicial = 0;
  saldoAtual = 0;
  saldoPrevisto = 0;

  saldoBaseContas = 0;

  totalDespesasPendentes = 0;
  totalReceitasPendentes = 0;
  totalTransferencias = 0;

  totalRecebido = 0;
  totalGasto = 0;

  ngOnInit(): void {
    this.contaService.listarTodos().subscribe(contas => {
      this.saldoBaseContas = contas.reduce((sum, c) => sum + (c.saldoInicial || 0), 0);
      this.carregarDados();
    });
  }

  carregarDados() {
    const ano = this.mesAtual.getFullYear();
    const mes = this.mesAtual.getMonth() + 1;

    this.lancamentoService.listarPorMesAno(ano, mes).subscribe(data => {
      this.lancamentos = data;
      this.calcularResumo();
    });
  }

  calcularResumo() {
    this.totalDespesasPendentes = 0;
    this.totalReceitasPendentes = 0;
    this.totalTransferencias = 0;
    this.totalRecebido = 0;
    this.totalGasto = 0;

    this.saldoInicial = this.saldoBaseContas;
    this.saldoAtual = this.saldoBaseContas;

    this.lancamentos.forEach(l => {
      const valor = typeof l.valor === 'string' ? parseFloat(l.valor) : l.valor;

      if (l.tipo === TipoLancamento.DESPESA) {
        if (l.status === StatusLancamento.PENDENTE) {
          this.totalDespesasPendentes += valor;
        } else {
          this.totalGasto += valor;
          this.saldoAtual -= valor;
        }
      } else if (l.tipo === TipoLancamento.RECEITA) {
        if (l.status === StatusLancamento.PENDENTE) {
          this.totalReceitasPendentes += valor;
        } else {
          this.totalRecebido += valor;
          this.saldoAtual += valor;
        }
      } else if (l.tipo === TipoLancamento.TRANSFERENCIA) {
        this.totalTransferencias += valor;
      }
    });

    this.saldoPrevisto = this.saldoAtual - this.totalDespesasPendentes + this.totalReceitasPendentes;

    this.ultimasDespesas = this.lancamentos
      .filter(l => l.tipo === TipoLancamento.DESPESA)
      .sort((a, b) => new Date(b.dataLancamento).getTime() - new Date(a.dataLancamento).getTime())
      .slice(0, 5);

    this.ultimasReceitas = this.lancamentos
      .filter(l => l.tipo === TipoLancamento.RECEITA)
      .sort((a, b) => new Date(b.dataLancamento).getTime() - new Date(a.dataLancamento).getTime())
      .slice(0, 5);
  }

  mudarMes(delta: number) {
    this.mesAtual = new Date(this.mesAtual.getFullYear(), this.mesAtual.getMonth() + delta, 1);
    this.carregarDados();
  }

  getNomeMesAno(): string {
    const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    return `${meses[this.mesAtual.getMonth()]} ${this.mesAtual.getFullYear()}`;
  }

  abrirModalNovoLancamento(tipo: 'DESPESA' | 'RECEITA' | 'TRANSFERENCIA') {
    const dialogRef = this.dialog.open(LancamentoModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { tipo }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.carregarDados();
      }
    });
  }

  abrirModalEdicaoLancamento(lancamento: Lancamento) {
    const dialogRef = this.dialog.open(LancamentoModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { tipo: lancamento.tipo, lancamento }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.carregarDados();
      }
    });
  }

  excluirLancamento(lancamento: Lancamento, event: Event) {
    event.stopPropagation();

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
      data: {
        titulo: 'Excluir Lançamento',
        mensagem: `Deseja realmente excluir o lançamento "${lancamento.descricao}"? Esta ação não pode ser desfeita.`
      }
    });

    dialogRef.afterClosed().subscribe(confirmado => {
      if (confirmado && lancamento.id) {
        this.lancamentoService.excluir(lancamento.id).subscribe({
          next: () => {
            this.carregarDados();
          },
          error: err => console.error(err)
        });
      }
    });
  }
}
