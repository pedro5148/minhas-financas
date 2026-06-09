import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { CartaoCredito, Fatura, Lancamento } from '../../../../models/types';
import { LancamentoService } from '../../../../services/lancamento.service';
import { FaturaService } from '../../../../services/fatura.service';
import { LancamentoModalComponent } from '../../../../components/lancamento-modal/lancamento-modal.component';

@Component({
  selector: 'app-fatura-modal',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule, MatDividerModule, MatChipsModule],
  templateUrl: './fatura-modal.component.html',
  styleUrl: './fatura-modal.component.scss'
})
export class FaturaModalComponent implements OnInit {
  private lancamentoService = inject(LancamentoService);
  private faturaService = inject(FaturaService);
  private dialog = inject(MatDialog);

  despesas: Lancamento[] = [];
  carregando = true;

  constructor(
    public dialogRef: MatDialogRef<FaturaModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { fatura: Fatura, cartao: CartaoCredito }
  ) {}

  ngOnInit(): void {
    this.carregarDespesas();
  }

  carregarDespesas() {
    this.carregando = true;
    if (this.data.fatura && this.data.fatura.id) {
      this.lancamentoService.listarPorFatura(this.data.fatura.id).subscribe(despesas => {
        this.despesas = despesas.sort((a, b) => new Date(b.dataLancamento).getTime() - new Date(a.dataLancamento).getTime());
        this.carregando = false;
      });
    }
  }

  pagarFatura() {
    if (!this.data.fatura.id) return;
    
    if (confirm('Tem certeza que deseja pagar o valor total desta fatura? O valor será debitado da sua conta padrão.')) {
      this.faturaService.pagarFatura(this.data.fatura.id).subscribe({
        next: (faturaAtualizada) => {
          this.data.fatura = faturaAtualizada;
          this.dialogRef.close(true); // Retorna true para recarregar a lista
        },
        error: (err) => {
          alert(err.error?.message || 'Erro ao pagar fatura');
        }
      });
    }
  }

  abrirEdicao(despesa: Lancamento) {
    const dialogRef = this.dialog.open(LancamentoModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { tipo: despesa.tipo, lancamento: despesa }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.carregarDespesas();
      }
    });
  }

  fechar() {
    this.dialogRef.close();
  }
}
