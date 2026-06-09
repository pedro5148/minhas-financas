import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { ContaService } from '../../services/conta.service';
import { Conta } from '../../models/types';

@Component({
  selector: 'app-conta-modal',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    MatDialogModule, 
    MatFormFieldModule,
    MatInputModule, 
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSlideToggleModule
  ],
  templateUrl: './conta-modal.component.html',
  styleUrl: './conta-modal.component.scss'
})
export class ContaModalComponent implements OnInit {
  form!: FormGroup;
  salvando = false;
  contaAtual: Conta | null = null;

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ContaModalComponent>);
  private contaService = inject(ContaService);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { conta: Conta | null }) {
    this.contaAtual = data.conta;
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: [this.contaAtual?.nome || 'Conta Principal', Validators.required],
      saldoInicial: [this.contaAtual?.saldoInicial || 0, Validators.required],
      padrao: [this.contaAtual?.padrao || false]
    });
  }

  salvar() {
    if (this.form.invalid) return;

    this.salvando = true;
    
    const formValues = this.form.value;
    const contaToSave: Conta = {
      ...(this.contaAtual || {}),
      nome: formValues.nome,
      saldoInicial: formValues.saldoInicial,
      padrao: formValues.padrao,
      dataCriacao: this.contaAtual?.dataCriacao || new Date().toISOString().split('T')[0]
    };

    const request$ = contaToSave.id 
      ? this.contaService.atualizar(contaToSave.id, contaToSave)
      : this.contaService.criar(contaToSave);

    request$.subscribe({
      next: (res) => {
        this.salvando = false;
        this.dialogRef.close(res);
      },
      error: (err) => {
        console.error(err);
        this.salvando = false;
      }
    });
  }

  cancelar() {
    this.dialogRef.close();
  }
}
