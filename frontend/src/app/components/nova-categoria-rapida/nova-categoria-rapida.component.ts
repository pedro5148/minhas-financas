import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';

import { CategoriaService } from '../../services/categoria.service';

@Component({
  selector: 'app-nova-categoria-rapida',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatSnackBarModule
  ],
  template: `
    <h2 mat-dialog-title>Nova Categoria Rápida</h2>
    <mat-dialog-content>
      <div style="margin-top: 16px;">
        <mat-form-field appearance="outline" style="width: 100%;">
          <mat-label>Nome da Categoria</mat-label>
          <input matInput [formControl]="nomeCtrl" placeholder="Ex: Alimentação" autocomplete="off">
          <mat-error *ngIf="nomeCtrl.hasError('required')">O nome é obrigatório</mat-error>
        </mat-form-field>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-raised-button color="primary" [disabled]="nomeCtrl.invalid || salvando()" (click)="salvar()">
        {{ salvando() ? 'Salvando...' : 'Salvar' }}
      </button>
    </mat-dialog-actions>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NovaCategoriaRapidaComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<NovaCategoriaRapidaComponent>);
  private categoriaService = inject(CategoriaService);
  private snackBar = inject(MatSnackBar);

  nomeCtrl = this.fb.control('', Validators.required);
  salvando = signal(false);

  salvar() {
    if (this.nomeCtrl.invalid) return;

    this.salvando.set(true);
    this.categoriaService.criarCategoria({ nome: this.nomeCtrl.value! }).subscribe({
      next: (novaCat) => {
        this.snackBar.open('Categoria criada com sucesso', 'Fechar', { duration: 3000 });
        this.dialogRef.close(novaCat);
      },
      error: () => {
        this.snackBar.open('Erro ao criar categoria', 'Fechar', { duration: 3000 });
        this.salvando.set(false);
      }
    });
  }
}
