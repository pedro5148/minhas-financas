import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-dashboard-resumo-saldos',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  templateUrl: './dashboard-resumo-saldos.component.html',
  styleUrl: './dashboard-resumo-saldos.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardResumoSaldosComponent {
  @Input({ required: true }) saldoInicial!: number;
  @Input({ required: true }) saldoAtual!: number;
  @Input({ required: true }) saldoPrevisto!: number;
}
