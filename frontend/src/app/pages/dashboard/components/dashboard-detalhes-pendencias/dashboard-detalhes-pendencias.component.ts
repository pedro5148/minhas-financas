import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dashboard-detalhes-pendencias',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  templateUrl: './dashboard-detalhes-pendencias.component.html',
  styleUrl: './dashboard-detalhes-pendencias.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardDetalhesPendenciasComponent {
  @Input({ required: true }) totalDespesasPendentes!: number;
  @Input({ required: true }) totalReceitasPendentes!: number;
  @Input({ required: true }) totalTransferencias!: number;
}
