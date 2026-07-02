import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, OnChanges, SimpleChanges } from '@angular/core';
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
export class DashboardUltimosLancamentosComponent implements OnChanges {
  @Input({ required: true }) titulo!: string;
  @Input({ required: true }) lancamentos: LancamentoResponseDTO[] = [];
  @Input({ required: true }) tipoBadge!: 'receita' | 'despesa';

  @Output() editar = new EventEmitter<LancamentoResponseDTO>();
  @Output() excluir = new EventEmitter<{lancamento: LancamentoResponseDTO, event: Event}>();

  sortColumn: keyof LancamentoResponseDTO | '' = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  sortedLancamentos: LancamentoResponseDTO[] = [];

  ngOnChanges(changes: SimpleChanges) {
    if (changes['lancamentos']) {
      this.applySort();
    }
  }

  sortBy(column: keyof LancamentoResponseDTO) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.applySort();
  }

  private applySort() {
    if (!this.sortColumn) {
      this.sortedLancamentos = [...this.lancamentos];
      return;
    }

    this.sortedLancamentos = [...this.lancamentos].sort((a, b) => {
      const valA = a[this.sortColumn as keyof LancamentoResponseDTO];
      const valB = b[this.sortColumn as keyof LancamentoResponseDTO];

      if (valA === valB) return 0;
      if (valA == null) return 1;
      if (valB == null) return -1;

      const comparison = valA > valB ? 1 : -1;
      return this.sortDirection === 'asc' ? comparison : -comparison;
    });
  }

  onRowClick(lancamento: LancamentoResponseDTO) {
    this.editar.emit(lancamento);
  }

  onDeleteClick(lancamento: LancamentoResponseDTO, event: Event) {
    event.stopPropagation();
    this.excluir.emit({ lancamento, event });
  }
}
