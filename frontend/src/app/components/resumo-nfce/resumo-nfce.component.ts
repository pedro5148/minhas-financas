import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { LancamentoResponseDTO, ItemLancamentoResponseDTO } from '../../models/lancamento.model';

@Component({
  selector: 'app-resumo-nfce',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatExpansionModule,
    MatIconModule,
    CurrencyPipe,
    DatePipe
  ],
  template: `
    <mat-card class="resumo-card" *ngIf="lancamento">
      <mat-card-header>
        <div mat-card-avatar class="resumo-avatar">
          <mat-icon>receipt_long</mat-icon>
        </div>
        <mat-card-title>{{ lancamento.estabelecimento?.nome || 'Estabelecimento' }}</mat-card-title>
        <mat-card-subtitle>{{ lancamento.dataLancamento | date:'shortDate' }}</mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        <div class="status-badge" [class.success]="true">
          <mat-icon>check_circle</mat-icon>
          <span>NFC-e processada com sucesso</span>
        </div>

        <mat-accordion>
          <mat-expansion-panel>
            <mat-expansion-panel-header>
              <mat-panel-title>
                Ver Itens ({{ getItens().length }})
              </mat-panel-title>
            </mat-expansion-panel-header>
            
            <div class="item-list">
              <div class="item-row" *ngFor="let item of getItens()">
                <div class="item-name">
                  <span class="qtd">{{ item.quantidade }}x</span> {{ item.produto.nome }}
                </div>
                <div class="item-price">{{ item.valorTotalBruto | currency:'BRL' }}</div>
              </div>
            </div>
          </mat-expansion-panel>
        </mat-accordion>

        <div class="math-summary">
          <div class="math-row">
            <span>Soma dos Itens (Prateleira)</span>
            <span>{{ lancamento.valorBruto | currency:'BRL' }}</span>
          </div>
          <div class="math-row discount" *ngIf="(lancamento.valorDesconto || 0) > 0">
            <span>Descontos na Nota</span>
            <span>- {{ lancamento.valorDesconto | currency:'BRL' }}</span>
          </div>
          <div class="math-row total">
            <span>Total Pago</span>
            <span>{{ lancamento.valor | currency:'BRL' }}</span>
          </div>
        </div>

        <div class="math-actions">
          <a class="rateio-link" (click)="onRateioClick()">Ratear desconto manualmente</a>
        </div>
      </mat-card-content>

      <mat-card-actions align="end">
        <button mat-button (click)="cancelar.emit()">Cancelar</button>
        <button mat-raised-button color="primary" (click)="confirmar.emit(lancamento)">Confirmar e Salvar</button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    .resumo-card { margin-top: 16px; }
    .resumo-avatar { 
      background: #e3f2fd; 
      display: flex; align-items: center; justify-content: center; 
      border-radius: 50%; color: #1976d2; 
    }
    .status-badge {
      display: flex; align-items: center; gap: 8px;
      padding: 12px; border-radius: 8px; margin: 16px 0;
      background: #e8f5e9; color: #2e7d32; font-weight: 500;
    }
    .status-badge mat-icon { font-size: 20px; height: 20px; width: 20px; }
    .item-list { display: flex; flex-direction: column; gap: 8px; }
    .item-row { display: flex; justify-content: space-between; font-size: 14px; }
    .item-name .qtd { font-weight: bold; color: #666; margin-right: 4px; }
    .math-summary {
      margin-top: 24px; padding: 16px;
      background: #f8f9fa; border-radius: 8px;
      display: flex; flex-direction: column; gap: 8px;
    }
    .math-row { display: flex; justify-content: space-between; color: #666; }
    .math-row.discount { color: #2e7d32; font-weight: 500; }
    .math-row.total {
      margin-top: 8px; padding-top: 8px; border-top: 1px solid #ddd;
      color: #000; font-size: 18px; font-weight: bold;
    }
    .math-actions { text-align: center; margin-top: 16px; }
    .rateio-link {
      color: #1976d2; font-size: 12px; text-decoration: underline;
      cursor: pointer;
    }
  `]
})
export class ResumoNfceComponent {
  @Input() lancamento!: LancamentoResponseDTO;
  @Output() confirmar = new EventEmitter<LancamentoResponseDTO>();
  @Output() cancelar = new EventEmitter<void>();

  getItens(): ItemLancamentoResponseDTO[] {
    // Para simplificar no modelo sem itens explicitamente tipados no DTO root, iteramos via any
    const lanc: any = this.lancamento;
    return lanc.itens || [];
  }

  onRateioClick() {
    alert("Funcionalidade de rateio manual de descontos em desenvolvimento.");
  }
}
