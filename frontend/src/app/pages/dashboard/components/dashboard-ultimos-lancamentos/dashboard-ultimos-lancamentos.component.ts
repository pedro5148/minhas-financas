import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LancamentoResponseDTO } from '../../../../models/lancamento.model';

@Component({
  selector: 'app-dashboard-ultimos-lancamentos',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  templateUrl: './dashboard-ultimos-lancamentos.component.html',
  styleUrl: './dashboard-ultimos-lancamentos.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardUltimosLancamentosComponent {
  @Input({ required: true }) titulo!: string;
  @Input({ required: true }) lancamentos: LancamentoResponseDTO[] = [];
  @Input({ required: true }) tipoBadge!: 'receita' | 'despesa';

  @Output() editar = new EventEmitter<LancamentoResponseDTO>();
  @Output() excluir = new EventEmitter<{lancamento: LancamentoResponseDTO, event: Event}>();

  onRowClick(lancamento: LancamentoResponseDTO) {
    this.editar.emit(lancamento);
  }

  onDeleteClick(lancamento: LancamentoResponseDTO, event: Event) {
    event.stopPropagation();
    this.excluir.emit({ lancamento, event });
  }
}
