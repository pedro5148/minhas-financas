import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

import { CategoriaService } from '../../services/categoria.service';

@Component({
  selector: 'app-categoria-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule
  ],
  templateUrl: './categoria-modal.component.html',
  styleUrl: './categoria-modal.component.scss'
})
export class CategoriaModalComponent implements OnInit {
  form!: FormGroup;
  salvando = false;

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<CategoriaModalComponent>);
  private categoriaService = inject(CategoriaService);

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: ['', Validators.required],
      subcategoriaNome: ['']
    });
  }

  salvar() {
    if (this.form.invalid) return;

    this.salvando = true;
    const values = this.form.value;

    this.categoriaService.criarCategoria(values).subscribe({
      next: (res) => {
        this.salvando = false;
        this.dialogRef.close(res);
      },
      error: (err) => {
        console.error('Erro ao criar categoria', err);
        this.salvando = false;
      }
    });
  }

  cancelar() {
    this.dialogRef.close();
  }
}
