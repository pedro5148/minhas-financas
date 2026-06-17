import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';

import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BehaviorSubject } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ContaService } from '../../services/conta.service';
import { CategoriaService } from '../../services/categoria.service';
import { Conta } from '../../models/conta.model';
import { Categoria } from '../../models/categoria.model';

import { ContaModalComponent } from '../../components/conta-modal/conta-modal.component';
import { CategoriaModalComponent } from '../../components/categoria-modal/categoria-modal.component';
import { ImportacaoModalComponent } from '../../components/importacao-modal/importacao-modal.component';

@Component({
  selector: 'app-configuracoes',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatDialogModule
  ],
  templateUrl: './configuracoes.component.html',
  styleUrl: './configuracoes.component.scss'
})
export class ConfiguracoesComponent implements OnInit {
  private contaService = inject(ContaService);
  private categoriaService = inject(CategoriaService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  conta: Conta | null = null;
  carregando = true;

  categorias: Categoria[] = [];
  carregandoCategorias = true;

  private reloadContaTrigger = new BehaviorSubject<void>(undefined);
  private reloadCategoriasTrigger = new BehaviorSubject<void>(undefined);

  ngOnInit() {
    this.reloadContaTrigger.pipe(
      takeUntilDestroyed(this.destroyRef),
      tap(() => this.carregando = true),
      switchMap(() => this.contaService.listarTodos())
    ).subscribe({
      next: (contas) => {
        if (contas && contas.length > 0) {
          this.conta = contas[0];
        } else {
          this.conta = { nome: 'Conta Principal', saldoInicial: 0 };
        }
        this.carregando = false;
      },
      error: () => {
        this.snackBar.open('Erro ao carregar conta.', 'Fechar', { duration: 3000 });
        this.carregando = false;
      }
    });

    this.reloadCategoriasTrigger.pipe(
      takeUntilDestroyed(this.destroyRef),
      tap(() => this.carregandoCategorias = true),
      switchMap(() => this.categoriaService.listarCategorias())
    ).subscribe({
      next: (cats) => {
        this.categorias = cats;
        this.carregandoCategorias = false;
      },
      error: () => {
        this.snackBar.open('Erro ao carregar categorias.', 'Fechar', { duration: 3000 });
        this.carregandoCategorias = false;
      }
    });
  }

  carregarConta() {
    this.reloadContaTrigger.next();
  }

  abrirModalConta() {
    const dialogRef = this.dialog.open(ContaModalComponent, {
      width: '500px',
      data: { conta: this.conta },
      panelClass: 'custom-dialog-container',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
      if (result) {
        this.snackBar.open('Conta atualizada com sucesso!', 'Fechar', { duration: 3000 });
        this.carregarConta();
      }
    });
  }

  carregarCategorias() {
    this.reloadCategoriasTrigger.next();
  }


  abrirModalCategoria() {
    const dialogRef = this.dialog.open(CategoriaModalComponent, {
      width: '500px',
      panelClass: 'custom-dialog-container',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
      if (result) {
        this.snackBar.open('Categoria criada com sucesso!', 'Fechar', { duration: 3000 });
        this.carregarCategorias();
      }
    });
  }

  abrirModalEdicaoCategoria(categoria: Categoria) {
    const dialogRef = this.dialog.open(CategoriaModalComponent, {
      width: '550px',
      data: { categoria },
      panelClass: 'custom-dialog-container',
      autoFocus: false
    });

    dialogRef.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
      if (result) {
        this.carregarCategorias();
      }
    });
  }

  excluirCategoria(id: number) {
    if (confirm('Tem certeza que deseja excluir esta categoria?')) {
      this.categoriaService.excluirCategoria(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.categorias = this.categorias.filter(c => c.id !== id);
          this.snackBar.open('Categoria excluída com sucesso!', 'Fechar', { duration: 3000 });
        },
        error: (err) => {
          if (err.status === 409) {
            this.snackBar.open(err.error || 'Não é possível excluir: Categoria está vinculada.', 'Fechar', { duration: 5000 });
          } else {
            this.snackBar.open('Erro ao excluir categoria.', 'Fechar', { duration: 3000 });
          }
        }
      });
    }
  }

  abrirModalImportacao() {
    this.dialog.open(ImportacaoModalComponent, {
      width: '850px',
      maxWidth: '95vw',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      autoFocus: false
    });
  }
}
