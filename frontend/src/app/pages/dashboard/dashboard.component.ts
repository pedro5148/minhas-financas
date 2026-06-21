import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, BehaviorSubject, merge } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';

import { LancamentoService } from '../../services/lancamento.service';
import { ContaService } from '../../services/conta.service';
import { LancamentoResponseDTO, TipoLancamento, StatusLancamento } from '../../models/lancamento.model';
import { LancamentoModalComponent } from '../../components/lancamento-modal/lancamento-modal.component';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { SeletorMesComponent, MesAno } from '../../components/seletor-mes/seletor-mes.component';

import { DashboardResumoSaldosComponent } from './components/dashboard-resumo-saldos/dashboard-resumo-saldos.component';
import { DashboardDetalhesPendenciasComponent } from './components/dashboard-detalhes-pendencias/dashboard-detalhes-pendencias.component';
import { DashboardUltimosLancamentosComponent } from './components/dashboard-ultimos-lancamentos/dashboard-ultimos-lancamentos.component';
import { DashboardTotalizadorComponent } from './components/dashboard-totalizador/dashboard-totalizador.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    MatIconModule, 
    MatButtonModule, 
    MatTooltipModule, 
    MatDialogModule,
    DashboardResumoSaldosComponent,
    DashboardDetalhesPendenciasComponent,
    DashboardUltimosLancamentosComponent,
    DashboardTotalizadorComponent,
    SeletorMesComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private lancamentoService = inject(LancamentoService);
  private contaService = inject(ContaService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  mesAtual: Date = new Date();

  lancamentos: LancamentoResponseDTO[] = [];
  ultimasDespesas: LancamentoResponseDTO[] = [];
  ultimasReceitas: LancamentoResponseDTO[] = [];

  saldoInicial = 0;
  saldoAtual = 0;
  saldoPrevisto = 0;

  saldoBaseContas = 0;

  totalDespesasPendentes = 0;
  totalReceitasPendentes = 0;
  totalTransferencias = 0;

  totalRecebido = 0;
  totalGasto = 0;

  menuAberto: boolean = false;

  private reloadTrigger = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    merge(
      this.reloadTrigger,
      this.lancamentoService.lancamentoAlterado$
    )
    .pipe(
      takeUntilDestroyed(this.destroyRef),
      switchMap(() => {
        const ano = this.mesAtual.getFullYear();
        const mes = this.mesAtual.getMonth() + 1;
        
        return forkJoin({
          contas: this.contaService.listarTodos(),
          lancamentos: this.lancamentoService.listarPorMesAno(ano, mes)
        });
      })
    )
    .subscribe(({ contas, lancamentos }) => {
      this.saldoBaseContas = contas.reduce((sum, c) => sum + (c.saldoInicial || 0), 0);
      this.lancamentos = [...lancamentos];
      this.calcularResumo();
    });
  }

  carregarDados() {
    this.reloadTrigger.next();
  }

  calcularResumo() {
    let despesasPendentesCents = 0;
    let receitasPendentesCents = 0;
    let transferenciasCents = 0;
    let recebidoCents = 0;
    let gastoCents = 0;
    let saldoAtualCents = Math.round(this.saldoBaseContas * 100);

    this.lancamentos.forEach(l => {
      const valorCents = Math.round(l.valor * 100);

      if (l.tipo === TipoLancamento.DESPESA) {
        if (l.status === StatusLancamento.PENDENTE) {
          if (!l.fatura) {
            despesasPendentesCents += valorCents;
          }
        } else {
          gastoCents += valorCents;
          if (!l.fatura) {
            saldoAtualCents -= valorCents;
          }
        }
      } else if (l.tipo === TipoLancamento.RECEITA) {
        if (l.status === StatusLancamento.PENDENTE) {
          receitasPendentesCents += valorCents;
        } else {
          recebidoCents += valorCents;
          saldoAtualCents += valorCents;
        }
      } else if (l.tipo === TipoLancamento.TRANSFERENCIA) {
        transferenciasCents += valorCents;
      }
    });

    // Para o saldo previsto, subtraímos as despesas pendentes (apenas as que não são de cartão, pois as do cartão só caem na fatura)
    let despesasPendentesNaoCartaoCents = 0;
    this.lancamentos.forEach(l => {
      if (l.tipo === TipoLancamento.DESPESA && l.status === StatusLancamento.PENDENTE && !l.fatura) {
        despesasPendentesNaoCartaoCents += Math.round(l.valor * 100);
      }
    });

    this.totalDespesasPendentes = despesasPendentesCents / 100;
    this.totalReceitasPendentes = receitasPendentesCents / 100;
    this.totalTransferencias = transferenciasCents / 100;
    this.totalRecebido = recebidoCents / 100;
    this.totalGasto = gastoCents / 100;

    this.saldoInicial = this.saldoBaseContas;
    this.saldoAtual = saldoAtualCents / 100;

    this.saldoPrevisto = (saldoAtualCents - despesasPendentesNaoCartaoCents + receitasPendentesCents) / 100;

    this.ultimasDespesas = [
      ...this.lancamentos
        .filter(l => l.tipo === TipoLancamento.DESPESA)
        .sort((a, b) => new Date(b.dataLancamento).getTime() - new Date(a.dataLancamento).getTime())
        .slice(0, 5)
    ];

    this.ultimasReceitas = [
      ...this.lancamentos
        .filter(l => l.tipo === TipoLancamento.RECEITA)
        .sort((a, b) => new Date(b.dataLancamento).getTime() - new Date(a.dataLancamento).getTime())
        .slice(0, 5)
    ];
  }

  onMesAlterado(evento: MesAno) {
    if (evento && evento.mes !== null) {
      this.mesAtual = new Date(evento.ano, evento.mes - 1, 1);
      this.carregarDados();
    }
  }

  abrirModalNovoLancamento(tipo: 'DESPESA' | 'RECEITA' | 'TRANSFERENCIA') {
    this.dialog.open(LancamentoModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { tipo }
    });
  }

  abrirModalEdicaoLancamento(lancamento: LancamentoResponseDTO) {
    this.dialog.open(LancamentoModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { tipo: lancamento.tipo, lancamento }
    });
  }

  excluirLancamento({ lancamento, event }: { lancamento: LancamentoResponseDTO, event: Event }) {
    event.stopPropagation();

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
      data: {
        titulo: 'Excluir Lançamento',
        mensagem: `Deseja realmente excluir o lançamento "${lancamento.descricao}"? Esta ação não pode ser desfeita.`
      }
    });

    dialogRef.afterClosed()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        filter(confirmado => confirmado === true && !!lancamento.id),
        switchMap(() => this.lancamentoService.excluir(lancamento.id!))
      )
      .subscribe({
        next: () => {
          this.lancamentoService.notificarAlteracao();
        },
        error: err => console.error(err)
      });
  }

  toggleMenu() {
    this.menuAberto = !this.menuAberto;
  }
}
