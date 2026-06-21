import { Component, Input, OnInit, OnChanges, SimpleChanges, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatRippleModule } from '@angular/material/core';
import { BehaviorSubject } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CartaoCredito } from '../../../../models/cartao-credito.model';
import { Fatura } from '../../../../models/fatura.model';
import { FaturaService } from '../../../../services/fatura.service';
import { FaturaModalComponent } from '../fatura-modal/fatura-modal.component';

@Component({
  selector: 'app-cartao-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressBarModule, MatButtonModule, MatRippleModule],
  templateUrl: './cartao-card.component.html',
  styleUrl: './cartao-card.component.scss'
})
export class CartaoCardComponent implements OnInit, OnChanges {
  @Input() cartao!: CartaoCredito;
  @Input() mesAnoSelecionado!: { mes: number, ano: number } | null;

  private faturaService = inject(FaturaService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  faturaAtual: Fatura | null = null;
  limiteUsado: number = 0;
  limiteDisponivel: number = 0;
  percentualUsado: number = 0;

  private reloadTrigger = new BehaviorSubject<void>(undefined);

  ngOnInit() {
    this.carregarDadosIniciais();
  }

  ngOnChanges(changes: SimpleChanges) {
    if ((changes['cartao'] && !changes['cartao'].firstChange) || (changes['mesAnoSelecionado'] && !changes['mesAnoSelecionado'].firstChange)) {
      this.carregarDadosIniciais();
    }
  }

  carregarDadosIniciais() {
    const cartaoId = this.cartao?.id;
    if (cartaoId) {
      this.faturaService.buscarPorCartao(cartaoId).subscribe(faturas => {
        let faturaEncontrada = null;
        if (this.mesAnoSelecionado) {
          const mesAnoStr = `${this.mesAnoSelecionado.mes.toString().padStart(2, '0')}/${this.mesAnoSelecionado.ano}`;
          faturaEncontrada = faturas.find(f => f.mesAno === mesAnoStr);
        }
        this.faturaAtual = faturaEncontrada || faturas.find(f => f.status === 'ABERTA') || faturas[0] || null;
        this.calcularLimites(faturas);
      });
    }
  }

  carregarFaturaAtual() {
    this.carregarDadosIniciais();
  }

  calcularLimites(faturas: Fatura[]) {
    const limiteUsadoCents = faturas
      .filter(f => f.status !== 'PAGA')
      .reduce((sum, f) => sum + (Math.round(f.valorTotal * 100) - Math.round(f.valorPago * 100)), 0);

    const limiteTotalCents = Math.round(this.cartao.limiteTotal * 100);
    const limiteDisponivelCents = limiteTotalCents - limiteUsadoCents;

    this.limiteUsado = limiteUsadoCents / 100;
    this.limiteDisponivel = limiteDisponivelCents / 100;
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

    dialogRef.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
      if (result) {
        this.carregarFaturaAtual();
      }
    });
  }
}
