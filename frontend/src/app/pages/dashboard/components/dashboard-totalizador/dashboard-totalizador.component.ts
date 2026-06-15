import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-dashboard-totalizador',
  standalone: true,
  imports: [CommonModule, MatCardModule],
  templateUrl: './dashboard-totalizador.component.html',
  styleUrl: './dashboard-totalizador.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardTotalizadorComponent {
  @Input({ required: true }) totalRecebido!: number;
  @Input({ required: true }) totalGasto!: number;
}
