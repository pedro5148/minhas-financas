import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CartaoCreditoService } from '../../../../services/cartao-credito.service';
import { ContaService } from '../../../../services/conta.service';
import { Conta } from '../../../../models/conta.model';
import { CartaoCredito, CartaoCreditoRequestDTO } from '../../../../models/cartao-credito.model';

@Component({
  selector: 'app-cartao-modal',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatCheckboxModule
  ],
  templateUrl: './cartao-modal.component.html',
  styleUrl: './cartao-modal.component.scss'
})
export class CartaoModalComponent implements OnInit {
  form!: FormGroup;
  contas: Conta[] = [];
  
  private fb = inject(FormBuilder);
  private cartaoService = inject(CartaoCreditoService);
  private contaService = inject(ContaService);
  private dialogRef = inject(MatDialogRef<CartaoModalComponent>);

  cartaoAtual: CartaoCredito | null = null;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { cartao?: CartaoCredito }) {
    if (data?.cartao) {
      this.cartaoAtual = data.cartao;
    }
  }

  ngOnInit() {
    this.carregarContas();
    this.inicializarFormulario();
  }

  carregarContas() {
    this.contaService.listarTodos().subscribe(contas => {
      this.contas = contas;
      if (this.cartaoAtual?.contaPadrao) {
        this.form.get('contaPadrao')?.setValue(this.contas.find(c => c.id === this.cartaoAtual!.contaPadrao.id) || null);
      } else {
        const padrao = this.contas.find(c => c.padrao);
        if (padrao) {
          this.form.get('contaPadrao')?.setValue(padrao);
        }
      }
    });
  }

  inicializarFormulario() {
    this.form = this.fb.group({
      nome: [this.cartaoAtual?.nome || '', Validators.required],
      limiteTotal: [this.cartaoAtual?.limiteTotal || null, [Validators.required, Validators.min(0)]],
      bandeira: [this.cartaoAtual?.bandeira || 'Mastercard', Validators.required],
      contaPadrao: [null, Validators.required],
      diaFechamento: [this.cartaoAtual?.diaFechamento || 1, [Validators.required, Validators.min(1), Validators.max(31)]],
      diaVencimento: [this.cartaoAtual?.diaVencimento || 5, [Validators.required, Validators.min(1), Validators.max(31)]],
      principal: [this.cartaoAtual?.principal || false]
    });
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const values = this.form.value;
    const cartaoRequest: CartaoCreditoRequestDTO = {
      nome: values.nome,
      limiteTotal: values.limiteTotal,
      bandeira: values.bandeira,
      contaPadraoId: values.contaPadrao.id,
      diaFechamento: values.diaFechamento,
      diaVencimento: values.diaVencimento,
      principal: values.principal
    };

    const req$ = this.cartaoAtual?.id 
      ? this.cartaoService.atualizar(this.cartaoAtual.id, cartaoRequest)
      : this.cartaoService.criar(cartaoRequest);

    req$.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err) => alert(err.error?.message || 'Erro ao salvar o cartão')
    });
  }

  cancelar() {
    this.dialogRef.close();
  }
}
