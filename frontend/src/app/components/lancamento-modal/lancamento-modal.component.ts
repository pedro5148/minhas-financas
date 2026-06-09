import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';
import { MatDialog } from '@angular/material/dialog';

import { LancamentoService } from '../../services/lancamento.service';
import { ContaService } from '../../services/conta.service';
import { CategoriaService } from '../../services/categoria.service';
import { CartaoCreditoService } from '../../services/cartao-credito.service';
import { Conta, Categoria, Subcategoria, TipoLancamento, TipoRecorrencia, StatusLancamento, Lancamento, CartaoCredito } from '../../models/types';

@Component({
  selector: 'app-lancamento-modal',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatDatepickerModule, MatNativeDateModule,
    MatButtonModule, MatRadioModule, MatSnackBarModule
  ],
  providers: [
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' }
  ],
  templateUrl: './lancamento-modal.component.html',
  styleUrl: './lancamento-modal.component.scss'
})
export class LancamentoModalComponent implements OnInit {
  form!: FormGroup;
  tipo: TipoLancamento;
  contas: Conta[] = [];
  categorias: Categoria[] = [];
  subcategorias: Subcategoria[] = [];
  subcategoriasFiltradas: Subcategoria[] = [];
  cartoes: CartaoCredito[] = [];

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<LancamentoModalComponent>);
  private lancamentoService = inject(LancamentoService);
  private contaService = inject(ContaService);
  private categoriaService = inject(CategoriaService);
  private cartaoService = inject(CartaoCreditoService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  lancamentoAtual: Lancamento | null = null;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { tipo: TipoLancamento, lancamento?: Lancamento }) {
    this.tipo = data.tipo;
    this.lancamentoAtual = data.lancamento || null;
  }

  ngOnInit(): void {
    this.inicializarFormulario();
    this.carregarDadosBase();
  }

  inicializarFormulario() {
    const parseDate = (d: any) => d ? new Date(d + 'T12:00:00') : null;

    this.form = this.fb.group({
      descricao: [this.lancamentoAtual?.descricao || '', Validators.required],
      valor: [this.lancamentoAtual?.valor || null, [Validators.required, Validators.min(0.01)]],
      conta: [null, Validators.required],
      cartaoCredito: [null],
      contaDestino: [null],
      categoria: [null],
      subcategoria: [null],
      dataLancamento: [this.lancamentoAtual ? parseDate(this.lancamentoAtual.dataLancamento) : new Date(), Validators.required],
      dataVencimento: [this.lancamentoAtual ? parseDate(this.lancamentoAtual.dataVencimento) : new Date(), Validators.required],
      dataEfetivacao: [this.lancamentoAtual?.dataEfetivacao ? parseDate(this.lancamentoAtual.dataEfetivacao) : null],
      observacoes: [this.lancamentoAtual?.observacoes || ''],
      tipoRecorrencia: [{ value: this.lancamentoAtual?.tipoRecorrencia || TipoRecorrencia.NENHUMA, disabled: !!this.lancamentoAtual }, Validators.required],
      totalParcelas: [{ value: this.lancamentoAtual?.totalParcelas || null, disabled: !!this.lancamentoAtual }],
      status: [this.lancamentoAtual?.status || StatusLancamento.PENDENTE, Validators.required]
    });

    if (this.tipo === TipoLancamento.TRANSFERENCIA) {
      this.form.get('contaDestino')?.setValidators(Validators.required);
    } else {
      this.form.get('categoria')?.setValidators(Validators.required);
    }

    this.form.get('categoria')?.valueChanges.subscribe(cat => {
      if (cat) {
        this.subcategoriasFiltradas = this.subcategorias.filter(s => s.categoria.id === cat.id);
      } else {
        this.subcategoriasFiltradas = [];
      }
      this.form.get('subcategoria')?.setValue(null);
    });

    this.form.get('tipoRecorrencia')?.valueChanges.subscribe(rec => {
      if (rec === TipoRecorrencia.PARCELADO) {
        this.form.get('totalParcelas')?.setValidators([Validators.required, Validators.min(2)]);
      } else {
        this.form.get('totalParcelas')?.clearValidators();
      }
      this.form.get('totalParcelas')?.updateValueAndValidity();
    });

    this.form.get('dataEfetivacao')?.valueChanges.subscribe(data => {
      if (data && this.form.get('status')?.value !== StatusLancamento.EFETIVADO) {
        this.form.get('status')?.setValue(StatusLancamento.EFETIVADO);
      }
    });

    this.form.get('status')?.valueChanges.subscribe(status => {
      if (status === StatusLancamento.EFETIVADO) {
        this.form.get('dataEfetivacao')?.setValidators(Validators.required);
      } else {
        this.form.get('dataEfetivacao')?.clearValidators();
      }
      this.form.get('dataEfetivacao')?.updateValueAndValidity({ emitEvent: false });
    });

    this.form.get('cartaoCredito')?.valueChanges.subscribe(cartao => {
      if (cartao && cartao.contaPadrao) {
        const contaEncontrada = this.contas.find(c => c.id === cartao.contaPadrao.id);
        if (contaEncontrada) {
          this.form.get('conta')?.setValue(contaEncontrada);
        }
      }
    });
  }

  carregarDadosBase() {
    this.contaService.listarTodos().subscribe(res => {
      this.contas = res;
      
      if (this.lancamentoAtual?.conta) {
        this.form.get('conta')?.setValue(this.contas.find(c => c.id === this.lancamentoAtual!.conta.id) || null);
      } else if (!this.form.get('conta')?.value) {
        const contaPadrao = this.contas.find(c => c.padrao === true);
        if (contaPadrao) {
          this.form.get('conta')?.setValue(contaPadrao);
        }
      }

      if (this.lancamentoAtual?.contaDestino) {
        this.form.get('contaDestino')?.setValue(this.contas.find(c => c.id === this.lancamentoAtual!.contaDestino!.id) || null);
      }
    });

    if (this.tipo === TipoLancamento.DESPESA) {
      this.cartaoService.listarTodos().subscribe(res => {
        this.cartoes = res;
        if (this.lancamentoAtual?.cartaoCredito) {
          this.form.get('cartaoCredito')?.setValue(this.cartoes.find(c => c.id === this.lancamentoAtual!.cartaoCredito!.id) || null);
        }
      });
    }

    if (this.tipo !== TipoLancamento.TRANSFERENCIA) {
      this.categoriaService.listarCategorias().subscribe(res => {
        this.categorias = res;
        if (this.lancamentoAtual?.categoria) {
          this.form.get('categoria')?.setValue(this.categorias.find(c => c.id === this.lancamentoAtual!.categoria!.id) || null);
        }
      });
      this.categoriaService.listarSubcategorias().subscribe(res => {
        this.subcategorias = res;
        if (this.lancamentoAtual?.subcategoria) {
          const sub = this.subcategorias.find(s => s.id === this.lancamentoAtual!.subcategoria!.id);
          if (sub) {
            this.subcategoriasFiltradas = this.subcategorias.filter(s => s.categoria.id === sub.categoria.id);
            this.form.get('subcategoria')?.setValue(sub);
          }
        }
      });
    }
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      
      const statusValue = this.form.get('status')?.value;
      const dataEfetValue = this.form.get('dataEfetivacao')?.value;

      if (statusValue === StatusLancamento.EFETIVADO && !dataEfetValue) {
        this.dialog.open(AlertDialogComponent, {
          width: '400px',
          panelClass: 'custom-dialog-container',
          data: {
            titulo: 'Atenção: Data Obrigatória',
            mensagem: 'Para salvar um lançamento como Efetivado, é estritamente necessário que você informe a Data de Efetivação. Verifique os campos em vermelho e tente novamente.'
          }
        });
      } else {
        this.dialog.open(AlertDialogComponent, {
          width: '400px',
          panelClass: 'custom-dialog-container',
          data: {
            titulo: 'Campos Inválidos',
            mensagem: 'Alguns campos obrigatórios não foram preenchidos corretamente. Verifique os destaques em vermelho no formulário.'
          }
        });
      }
      return;
    }

    const values = this.form.getRawValue();
    const lancamento: any = {
      id: this.lancamentoAtual?.id,
      tipo: this.tipo,
      descricao: values.descricao,
      valor: values.valor,
      conta: values.conta,
      contaDestino: this.tipo === TipoLancamento.TRANSFERENCIA ? values.contaDestino : null,
      cartaoCredito: this.tipo === TipoLancamento.DESPESA ? values.cartaoCredito : null,
      categoria: this.tipo !== TipoLancamento.TRANSFERENCIA ? values.categoria : null,
      subcategoria: values.subcategoria,
      dataLancamento: values.dataLancamento.toISOString().split('T')[0],
      dataVencimento: values.dataVencimento.toISOString().split('T')[0],
      dataEfetivacao: values.dataEfetivacao ? values.dataEfetivacao.toISOString().split('T')[0] : null,
      observacoes: values.observacoes,
      tipoRecorrencia: values.tipoRecorrencia,
      totalParcelas: values.tipoRecorrencia === TipoRecorrencia.PARCELADO ? values.totalParcelas : null,
      status: values.status
    };

    const request$: import('rxjs').Observable<any> = this.lancamentoAtual?.id 
      ? this.lancamentoService.atualizar(this.lancamentoAtual.id, lancamento)
      : this.lancamentoService.criar(lancamento);

    request$.subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: any) => console.error(err)
    });
  }

  cancelar() {
    this.dialogRef.close();
  }
}
