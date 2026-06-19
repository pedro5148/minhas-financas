import { Component, ChangeDetectionStrategy, OnInit, ViewChild, AfterViewInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule, MatTabChangeEvent } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { Subject, merge } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { LancamentoResponseDTO, TipoLancamento } from '../../models/lancamento.model';
import { LancamentoService } from '../../services/lancamento.service';
import { LancamentoModalComponent } from '../../components/lancamento-modal/lancamento-modal.component';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';
import { SeletorMesComponent, MesAno } from '../../components/seletor-mes/seletor-mes.component';

@Component({
  selector: 'app-lancamentos-lista',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatChipsModule,
    SeletorMesComponent
  ],
  templateUrl: './lancamentos-lista.component.html',
  styleUrl: './lancamentos-lista.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LancamentosListaComponent implements OnInit, AfterViewInit {
  private lancamentoService = inject(LancamentoService);

  displayedColumns: string[] = ['dataLancamento', 'descricao', 'categoria', 'conta', 'cartao', 'valor', 'acoes'];
  dataSource = new MatTableDataSource<LancamentoResponseDTO>([]);
  
  tiposLancamento = TipoLancamento;
  totalElements = 0;
  filtroTipo: string | null = null;
  
  dataAtualParaSeletor = new Date();
  filtroMes: number | null = this.dataAtualParaSeletor.getMonth() + 1;
  filtroAno: number | null = this.dataAtualParaSeletor.getFullYear();

  private searchSubject = new Subject<string>();
  private dialog = inject(MatDialog);

  onTabChange(event: MatTabChangeEvent) {
    if (event.index === 0) {
      this.filtroTipo = null;
    } else if (event.index === 1) {
      this.filtroTipo = 'DESPESA';
    } else if (event.index === 2) {
      this.filtroTipo = 'RECEITA';
    }

    if (this.paginator) {
      this.paginator.pageIndex = 0;
    }
    
    this.carregarLancamentos();
  }

  onMesAlterado(evento: MesAno) {
    this.filtroMes = evento.mes;
    this.filtroAno = evento.ano;

    if (this.paginator) {
      this.paginator.pageIndex = 0;
    }
    this.carregarLancamentos();
  }

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit() {
    this.carregarLancamentos();
  }

  ngAfterViewInit() {
    this.sort.sortChange.subscribe(() => {
      if (this.paginator) {
        this.paginator.pageIndex = 0;
      }
    });

    merge(this.sort.sortChange, this.paginator.page)
      .subscribe(() => this.carregarLancamentos());

    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      if (this.paginator) {
        this.paginator.pageIndex = 0;
      }
      this.carregarLancamentos();
    });
  }

  carregarLancamentos() {
    const page = this.paginator ? this.paginator.pageIndex : 0;
    const size = this.paginator ? this.paginator.pageSize : 10;
    
    let sortStr = 'dataLancamento,desc';
    if (this.sort && this.sort.active && this.sort.direction) {
      sortStr = `${this.sort.active},${this.sort.direction}`;
    }

    const descricao = this.dataSource.filter || '';

    this.lancamentoService.listar(page, size, sortStr, descricao, this.filtroTipo, this.filtroMes, this.filtroAno).subscribe(pagina => {
      this.dataSource.data = pagina.content;
      this.totalElements = pagina.totalElements;
    });
  }

  aplicarFiltro(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    this.searchSubject.next(this.dataSource.filter);
  }

  abrirModalEdicao(lancamento: LancamentoResponseDTO) {
    const dialogRef = this.dialog.open(LancamentoModalComponent, {
      width: '800px',
      maxWidth: '95vw',
      data: { tipo: lancamento.tipo, lancamento }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.carregarLancamentos();
      }
    });
  }

  excluirLancamento(lancamento: LancamentoResponseDTO) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
      data: {
        titulo: 'Excluir Lançamento',
        mensagem: `Deseja realmente excluir o lançamento "${lancamento.descricao}"? Esta ação não pode ser desfeita.`
      }
    });

    dialogRef.afterClosed().subscribe(confirmado => {
      if (confirmado && lancamento.id) {
        this.lancamentoService.excluir(lancamento.id).subscribe(() => {
          this.carregarLancamentos();
        });
      }
    });
  }
}
