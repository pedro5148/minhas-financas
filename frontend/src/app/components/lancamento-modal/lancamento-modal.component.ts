import { Component, Inject, OnInit, inject, DestroyRef } from '@angular/core';
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
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';

import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';
import { MatDialog } from '@angular/material/dialog';

import { LancamentoService } from '../../services/lancamento.service';
import { ContaService } from '../../services/conta.service';
import { CategoriaService } from '../../services/categoria.service';
import { CartaoCreditoService } from '../../services/cartao-credito.service';
import { Conta } from '../../models/conta.model';
import { Categoria, Subcategoria } from '../../models/categoria.model';
import { TipoLancamento, TipoRecorrencia, StatusLancamento, LancamentoResponseDTO, LancamentoRequestDTO } from '../../models/lancamento.model';
import { CartaoCredito } from '../../models/cartao-credito.model';
import { FaturaService } from '../../services/fatura.service';
import { Fatura } from '../../models/fatura.model';

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
  faturasProjetadas: Fatura[] = [];

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<LancamentoModalComponent>);
  private lancamentoService = inject(LancamentoService);
  private contaService = inject(ContaService);
  private categoriaService = inject(CategoriaService);
  private cartaoService = inject(CartaoCreditoService);
  private faturaService = inject(FaturaService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  lancamentoAtual: LancamentoResponseDTO | null = null;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { tipo: TipoLancamento, lancamento?: LancamentoResponseDTO }) {
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
      fatura: [{value: null, disabled: true}],
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

    this.form.get('categoria')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(cat => {
      if (cat) {
        this.subcategoriasFiltradas = this.subcategorias.filter(s => s.categoria.id === cat.id);
      } else {
        this.subcategoriasFiltradas = [];
      }
      this.form.get('subcategoria')?.setValue(null);
    });

    this.form.get('tipoRecorrencia')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(rec => {
      if (rec === TipoRecorrencia.PARCELADO) {
        this.form.get('totalParcelas')?.setValidators([Validators.required, Validators.min(2)]);
      } else {
        this.form.get('totalParcelas')?.clearValidators();
      }
      this.form.get('totalParcelas')?.updateValueAndValidity();
    });

    this.form.get('dataEfetivacao')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(data => {
      if (data && this.form.get('status')?.value !== StatusLancamento.EFETIVADO) {
        this.form.get('status')?.setValue(StatusLancamento.EFETIVADO);
      }
    });

    this.form.get('status')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(status => {
      if (status === StatusLancamento.EFETIVADO) {
        this.form.get('dataEfetivacao')?.setValidators(Validators.required);
      } else {
        this.form.get('dataEfetivacao')?.clearValidators();
      }
      this.form.get('dataEfetivacao')?.updateValueAndValidity({ emitEvent: false });
    });

    this.form.get('cartaoCredito')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(cartao => {
      if (cartao && cartao.contaPadrao) {
        const contaEncontrada = this.contas.find(c => c.id === cartao.contaPadrao.id);
        if (contaEncontrada) {
          this.form.get('conta')?.setValue(contaEncontrada);
        }
      }

      if (cartao) {
        this.faturaService.projetarProximasFaturas(cartao.id).subscribe(faturas => {
          this.faturasProjetadas = faturas;
          this.form.get('fatura')?.enable();
          
          if (this.lancamentoAtual?.fatura) {
            const faturaEncontrada = this.faturasProjetadas.find(f => f.mesAno === this.lancamentoAtual!.fatura!.mesAno);
            this.form.get('fatura')?.setValue(faturaEncontrada || this.faturasProjetadas[0]);
          } else {
            this.form.get('fatura')?.setValue(this.faturasProjetadas[0]);
          }
        });
      } else {
        this.faturasProjetadas = [];
        this.form.get('fatura')?.setValue(null);
        this.form.get('fatura')?.disable();
      }
    });
  }

  carregarDadosBase() {
    const requisicoes: any = {
      contas: this.contaService.listarTodos()
    };

    if (this.tipo === TipoLancamento.DESPESA) {
      requisicoes.cartoes = this.cartaoService.listarTodos();
    }

    if (this.tipo !== TipoLancamento.TRANSFERENCIA) {
      requisicoes.categorias = this.categoriaService.listarCategorias();
      requisicoes.subcategorias = this.categoriaService.listarSubcategorias();
    }

    forkJoin(requisicoes)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res: any) => {
        // Tratar Contas
        this.contas = res.contas;
        if (this.lancamentoAtual?.conta) {
          this.form.get('conta')?.setValue(this.contas.find((c: Conta) => c.id === this.lancamentoAtual!.conta!.id) || null);
        } else if (!this.form.get('conta')?.value) {
          const contaPadrao = this.contas.find((c: Conta) => c.padrao === true);
          if (contaPadrao) {
            this.form.get('conta')?.setValue(contaPadrao);
          }
        }

        if (this.lancamentoAtual?.contaDestino) {
          this.form.get('contaDestino')?.setValue(this.contas.find((c: Conta) => c.id === this.lancamentoAtual!.contaDestino!.id) || null);
        }

        // Tratar Cartões
        if (res.cartoes) {
          this.cartoes = res.cartoes;
          if (this.lancamentoAtual?.cartaoCredito) {
            this.form.get('cartaoCredito')?.setValue(this.cartoes.find((c: CartaoCredito) => c.id === this.lancamentoAtual!.cartaoCredito!.id) || null);
          }
        }

        // Tratar Categorias e Subcategorias
        if (res.categorias && res.subcategorias) {
          this.categorias = res.categorias;
          if (this.lancamentoAtual?.categoria) {
            this.form.get('categoria')?.setValue(this.categorias.find((c: Categoria) => c.id === this.lancamentoAtual!.categoria!.id) || null);
          }

          this.subcategorias = res.subcategorias;
          if (this.lancamentoAtual?.subcategoria) {
            const sub = this.subcategorias.find((s: Subcategoria) => s.id === this.lancamentoAtual!.subcategoria!.id);
            if (sub) {
              this.subcategoriasFiltradas = this.subcategorias.filter((s: Subcategoria) => s.categoria.id === sub.categoria.id);
              this.form.get('subcategoria')?.setValue(sub);
            }
          }
        }
      });
  }

  private formatarData(data: Date): string {
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
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
    
    let faturaId = null;
    let mesFatura = null;
    let anoFatura = null;

    if (this.tipo === TipoLancamento.DESPESA && values.cartaoCredito && values.fatura) {
      if (values.fatura.id) {
        faturaId = values.fatura.id;
      } else if (values.fatura.mesAno) {
        const partes = values.fatura.mesAno.split('/');
        mesFatura = parseInt(partes[0], 10);
        anoFatura = parseInt(partes[1], 10);
      }
    }

    const lancamento: LancamentoRequestDTO = {
      tipo: this.tipo,
      descricao: values.descricao,
      valor: values.valor,
      contaId: values.conta?.id,
      contaDestinoId: this.tipo === TipoLancamento.TRANSFERENCIA ? values.contaDestino?.id : null,
      cartaoCreditoId: this.tipo === TipoLancamento.DESPESA ? values.cartaoCredito?.id : null,
      faturaId: faturaId,
      mesFatura: mesFatura,
      anoFatura: anoFatura,
      subcategoriaId: values.subcategoria?.id,
      dataLancamento: values.dataLancamento ? this.formatarData(values.dataLancamento) : '',
      dataVencimento: values.dataVencimento ? this.formatarData(values.dataVencimento) : '',
      dataEfetivacao: values.dataEfetivacao ? this.formatarData(values.dataEfetivacao) : null,
      observacoes: values.observacoes,
      tipoRecorrencia: values.tipoRecorrencia,
      totalParcelas: values.tipoRecorrencia === TipoRecorrencia.PARCELADO ? values.totalParcelas : null,
      status: values.status
    };

    const request$: import('rxjs').Observable<any> = this.lancamentoAtual?.id 
      ? this.lancamentoService.atualizar(this.lancamentoAtual.id, lancamento)
      : this.lancamentoService.criar(lancamento);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.snackBar.open('Lançamento salvo com sucesso!', 'Fechar', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['snackbar-success']
        });
        this.lancamentoService.notificarAlteracao();
        this.dialogRef.close(true);
      },
      error: (err: any) => console.error(err)
    });
  }

  cancelar() {
    this.dialogRef.close();
  }
}
