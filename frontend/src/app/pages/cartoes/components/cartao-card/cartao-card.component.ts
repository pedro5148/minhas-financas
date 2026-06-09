import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatRippleModule } from '@angular/material/core';
import { CartaoCredito, Fatura } from '../../../../models/types';
import { FaturaService } from '../../../../services/fatura.service';
import { FaturaModalComponent } from '../fatura-modal/fatura-modal.component';

@Component({
  selector: 'app-cartao-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressBarModule, MatButtonModule, MatRippleModule],
  templateUrl: './cartao-card.component.html',
  styleUrl: './cartao-card.component.scss'
})
export class CartaoCardComponent implements OnInit {
  @Input() cartao!: CartaoCredito;

  private faturaService = inject(FaturaService);
  private dialog = inject(MatDialog);

  faturaAtual: Fatura | null = null;
  limiteUsado: number = 0;
  limiteDisponivel: number = 0;
  percentualUsado: number = 0;

  ngOnInit() {
    this.carregarFaturaAtual();
  }

  carregarFaturaAtual() {
    if (this.cartao && this.cartao.id) {
      this.faturaService.buscarPorCartao(this.cartao.id).subscribe(faturas => {
        this.faturaAtual = faturas.find(f => f.status === 'ABERTA') || faturas[0] || null;
        this.calcularLimites(faturas);
      });
    }
  }

  calcularLimites(faturas: Fatura[]) {
    this.limiteUsado = faturas
      .filter(f => f.status !== 'PAGA')
      .reduce((sum, f) => sum + (f.valorTotal - f.valorPago), 0);

    this.limiteDisponivel = this.cartao.limiteTotal - this.limiteUsado;
    this.percentualUsado = (this.limiteUsado / this.cartao.limiteTotal) * 100;
  }

  abrirFatura() {
    if (!this.faturaAtual) return;

    const dialogRef = this.dialog.open(FaturaModalComponent, {
      width: '900px',
      maxWidth: '95vw',
      data: {
        fatura: this.faturaAtual,
        cartao: this.cartao
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.carregarFaturaAtual();
      }
    });
  }
}
