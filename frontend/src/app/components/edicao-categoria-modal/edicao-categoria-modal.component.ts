import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';

import { CategoriaService } from '../../services/categoria.service';
import { Categoria, Subcategoria } from '../../models/types';

@Component({
  selector: 'app-edicao-categoria-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatInputModule,
    MatTooltipModule
  ],
  templateUrl: './edicao-categoria-modal.component.html',
  styleUrl: './edicao-categoria-modal.component.scss'
})
export class EdicaoCategoriaModalComponent implements OnInit {
  categoriaForm!: FormGroup;
  subcategoriaForm!: FormGroup;

  subcategorias: Subcategoria[] = [];
  carregandoSubcategorias = true;
  salvandoCategoria = false;
  adicionandoSubcategoria = false;

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<EdicaoCategoriaModalComponent>);
  private categoriaService = inject(CategoriaService);
  private snackBar = inject(MatSnackBar);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { categoria: Categoria }) { }

  ngOnInit(): void {
    this.categoriaForm = this.fb.group({
      nome: [this.data.categoria.nome, Validators.required]
    });

    this.subcategoriaForm = this.fb.group({
      nome: ['', Validators.required]
    });

    this.carregarSubcategorias();
  }

  carregarSubcategorias() {
    this.carregandoSubcategorias = true;
    this.categoriaService.listarSubcategoriasPorCategoria(this.data.categoria.id!).subscribe({
      next: (subs) => {
        this.subcategorias = subs;
        this.carregandoSubcategorias = false;
      },
      error: () => {
        this.snackBar.open('Erro ao carregar subcategorias.', 'Fechar', { duration: 3000 });
        this.carregandoSubcategorias = false;
      }
    });
  }

  salvarCategoria() {
    if (this.categoriaForm.invalid) return;

    if (this.categoriaForm.pristine) {
      this.dialogRef.close(true);
      return;
    }

    this.salvandoCategoria = true;
    const values = this.categoriaForm.value;

    this.categoriaService.atualizarCategoria(this.data.categoria.id!, values).subscribe({
      next: (res) => {
        this.salvandoCategoria = false;
        this.snackBar.open('Categoria atualizada com sucesso!', 'Fechar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        console.error('Erro ao atualizar categoria', err);
        this.snackBar.open('Erro ao atualizar categoria.', 'Fechar', { duration: 3000 });
        this.salvandoCategoria = false;
      }
    });
  }

  adicionarSubcategoria() {
    if (this.subcategoriaForm.invalid) return;

    this.adicionandoSubcategoria = true;
    const nome = this.subcategoriaForm.get('nome')?.value;

    this.categoriaService.criarSubcategoria(this.data.categoria.id!, nome).subscribe({
      next: (novaSub) => {
        this.subcategorias.push(novaSub);
        this.subcategoriaForm.reset();
        this.adicionandoSubcategoria = false;
      },
      error: (err) => {
        console.error('Erro ao adicionar subcategoria', err);
        this.snackBar.open('Erro ao adicionar subcategoria.', 'Fechar', { duration: 3000 });
        this.adicionandoSubcategoria = false;
      }
    });
  }

  excluirSubcategoria(id: number) {
    if (confirm('Tem certeza que deseja excluir esta subcategoria?')) {
      this.categoriaService.excluirSubcategoria(id).subscribe({
        next: () => {
          this.subcategorias = this.subcategorias.filter(s => s.id !== id);
          this.snackBar.open('Subcategoria excluída!', 'Fechar', { duration: 2000 });
        },
        error: (err) => {
          if (err.status === 409) {
            this.snackBar.open(err.error || 'Subcategoria em uso.', 'Fechar', { duration: 5000 });
          } else {
            this.snackBar.open('Erro ao excluir subcategoria.', 'Fechar', { duration: 3000 });
          }
        }
      });
    }
  }

  fechar() {
    this.dialogRef.close();
  }
}
