import { Component, Inject, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin, of, Observable } from 'rxjs';
import { switchMap, catchError, debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';
import { MatDialog } from '@angular/material/dialog';

import { LancamentoService } from '../../services/lancamento.service';
import { ContaService } from '../../services/conta.service';
import { CategoriaService } from '../../services/categoria.service';
import { CartaoCreditoService } from '../../services/cartao-credito.service';
import { ProdutoService } from '../../services/produto.service';
import { Conta } from '../../models/conta.model';
import { Categoria, Subcategoria } from '../../models/categoria.model';
import { 
  TipoLancamento, 
  TipoRecorrencia, 
  StatusLancamento, 
  LancamentoResponseDTO, 
  LancamentoRequestDTO,
  NfceParseRequestDTO,
  NfceEfetivarRequestDTO,
  ProdutoResponseDTO
} from '../../models/lancamento.model';
import { CartaoCredito } from '../../models/cartao-credito.model';
import { FaturaService } from '../../services/fatura.service';
import { Fatura } from '../../models/fatura.model';
import { ScannerComponent } from '../../core/scanner/scanner.component';
import { ResumoNfceComponent } from '../resumo-nfce/resumo-nfce.component';
import { SefazScraperService } from '../../core/webview/sefaz-scraper.service';

@Component({
  selector: 'app-lancamento-modal',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatDatepickerModule, MatNativeDateModule,
    MatButtonModule, MatRadioModule, MatSnackBarModule, MatIconModule,
    ScannerComponent, ResumoNfceComponent
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
  
  filteredProducts: ProdutoResponseDTO[][] = [];

  // NFC-e flags
  podeSerNfce = false;
  mostrarScanner = false;
  mostrarResumoNfce = false;
  previewNfceData: LancamentoResponseDTO | null = null;

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<LancamentoModalComponent>);
  private lancamentoService = inject(LancamentoService);
  private contaService = inject(ContaService);
  private categoriaService = inject(CategoriaService);
  private cartaoService = inject(CartaoCreditoService);
  private faturaService = inject(FaturaService);
  private sefazScraper = inject(SefazScraperService);
  private produtoService = inject(ProdutoService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  lancamentoAtual: LancamentoResponseDTO | null = null;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { tipo: TipoLancamento, lancamento?: LancamentoResponseDTO }) {
    this.tipo = data.tipo;
    this.lancamentoAtual = data.lancamento || null;
  }

  get itens(): FormArray {
    return this.form.get('itens') as FormArray;
  }

  ngOnInit(): void {
    this.inicializarFormulario();
    this.carregarDadosBase();
  }

  inicializarFormulario() {
    const parseDate = (d: any) => d ? new Date(d + 'T12:00:00') : null;

    this.form = this.fb.group({
      entryMethod: ['simples'],
      descricao: [this.lancamentoAtual?.descricao || '', Validators.required],
      valor: [{value: this.lancamentoAtual?.valor || null, disabled: false}, [Validators.required, Validators.min(0.01)]],
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
      status: [this.lancamentoAtual?.status || StatusLancamento.PENDENTE, Validators.required],
      itens: this.fb.array([])
    });

    if (this.tipo === TipoLancamento.TRANSFERENCIA) {
      this.form.get('contaDestino')?.setValidators(Validators.required);
    } else {
      this.form.get('categoria')?.setValidators(Validators.required);
    }

    this.setupReactivity();
  }

  private setupReactivity() {
    this.form.get('entryMethod')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(method => {
      const valorCtrl = this.form.get('valor');
      if (method === 'manual') {
        valorCtrl?.disable();
        if (this.itens.length === 0) {
          this.addItem();
        }
      } else {
        valorCtrl?.enable();
        this.itens.clear();
        this.filteredProducts = [];
        
        if (method === 'qrcode') {
          this.iniciarScanner();
        }
      }
    });

    this.itens.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(itemsValue => {
      if (this.form.get('entryMethod')?.value === 'manual') {
        const sum = itemsValue.reduce((acc: number, item: any) => acc + ((item.quantidade || 0) * (item.valorUnitarioBruto || 0)), 0);
        this.form.get('valor')?.setValue(sum, { emitEvent: false });
      }
    });

    this.form.get('categoria')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(cat => {
      if (cat) {
        this.subcategoriasFiltradas = this.subcategorias.filter(s => s.categoria.id === cat.id);
        if (cat.permiteDetalhamento) {
          this.podeSerNfce = true;
        } else {
          this.podeSerNfce = false;
          this.resetarMetodoDeEntrada();
        }
        
        if (this.subcategoriasFiltradas.length > 0) {
          this.form.get('subcategoria')?.setValue(this.subcategoriasFiltradas[0]);
        } else {
          this.form.get('subcategoria')?.setValue(null);
        }
      } else {
        this.subcategoriasFiltradas = [];
        this.podeSerNfce = false;
        this.resetarMetodoDeEntrada();
        this.form.get('subcategoria')?.setValue(null);
      }
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

    this.form.get('dataLancamento')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(data => {
      if (data) {
        this.form.get('dataVencimento')?.setValue(data);
      }
    });

    this.form.get('status')?.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(status => {
      if (status === StatusLancamento.EFETIVADO) {
        this.form.get('dataEfetivacao')?.setValidators(Validators.required);
        if (!this.form.get('dataEfetivacao')?.value) {
          this.form.get('dataEfetivacao')?.setValue(this.form.get('dataLancamento')?.value);
        }
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

  private resetarMetodoDeEntrada(): void {
    if (this.form.get('entryMethod')?.value !== 'simples') {
      this.form.get('entryMethod')?.setValue('simples');
    }
  }

  addItem() {
    const itemGroup = this.fb.group({
      produtoNome: ['', Validators.required],
      produtoId: [null],
      quantidade: [1, [Validators.required, Validators.min(1)]],
      valorUnitarioBruto: [0, [Validators.required, Validators.min(0)]],
      valorTotalBruto: [{value: 0, disabled: true}]
    });

    const index = this.itens.length;
    this.itens.push(itemGroup);
    this.filteredProducts.push([]);

    itemGroup.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(val => {
      const totalItem = (val.quantidade || 0) * (val.valorUnitarioBruto || 0);
      itemGroup.get('valorTotalBruto')?.setValue(totalItem, { emitEvent: false });
    });

    itemGroup.get('produtoNome')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => {
        if (!term || term.length < 2) return of([]);
        return this.produtoService.search(term);
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(results => {
      this.filteredProducts[index] = results;
    });
  }

  removeItem(index: number) {
    this.itens.removeAt(index);
    this.filteredProducts.splice(index, 1);
  }

  selectProduct(index: number, produto: ProdutoResponseDTO) {
    const itemGroup = this.itens.at(index) as FormGroup;
    itemGroup.patchValue({
      produtoNome: produto.nome,
      produtoId: produto.id,
      valorUnitarioBruto: 0 
    });
    this.filteredProducts[index] = [];
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

        if (res.cartoes) {
          this.cartoes = res.cartoes;
          if (this.lancamentoAtual?.cartaoCredito) {
            this.form.get('cartaoCredito')?.setValue(this.cartoes.find((c: CartaoCredito) => c.id === this.lancamentoAtual!.cartaoCredito!.id) || null);
          }
        }

        if (res.categorias && res.subcategorias) {
          this.categorias = res.categorias;
          this.subcategorias = res.subcategorias;

          if (this.lancamentoAtual?.categoria) {
            this.form.get('categoria')?.setValue(this.categorias.find((c: Categoria) => c.id === this.lancamentoAtual!.categoria!.id) || null);
          }

          if (this.lancamentoAtual?.subcategoria) {
            const sub = this.subcategorias.find((s: Subcategoria) => s.id === this.lancamentoAtual!.subcategoria!.id);
            if (sub) {
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

  // ==== AÇÕES NFC-E ====
  iniciarScanner() {
    this.mostrarScanner = true;
  }

  fecharScanner() {
    this.mostrarScanner = false;
    if (!this.previewNfceData && this.form.get('entryMethod')?.value === 'qrcode') {
      this.form.get('entryMethod')?.setValue('simples', { emitEvent: false });
    }
  }

  onScanResult(url: string) {
    this.fecharScanner();
    this.snackBar.open('Extraindo itens da Nota Fiscal...', '', { duration: 3000 });
    
    const values = this.form.getRawValue();

    this.sefazScraper.extractHtmlFromSefaz(url).pipe(
      switchMap(htmlContent => {
        const parseReq: NfceParseRequestDTO = {
          htmlContent,
          contaId: values.conta?.id,
          categoriaId: values.categoria?.id,
          subcategoriaId: values.subcategoria?.id,
          dataPagamento: values.dataEfetivacao ? this.formatarData(values.dataEfetivacao) : null
        };
        return this.lancamentoService.previewNfce(parseReq);
      }),
      catchError(err => {
        console.error(err);
        this.snackBar.open('Erro ao processar a Nota Fiscal. Tente novamente ou use o lançamento manual.', 'Fechar', { duration: 5000 });
        this.form.get('entryMethod')?.setValue('simples', { emitEvent: false });
        return of(null);
      })
    ).subscribe(preview => {
      if (preview) {
        this.previewNfceData = preview;
        this.mostrarResumoNfce = true;
      }
    });
  }

  cancelarNfce() {
    this.mostrarResumoNfce = false;
    this.previewNfceData = null;
    this.form.get('entryMethod')?.setValue('simples', { emitEvent: false });
  }

  confirmarNfceEfetivacao(lancamentoValidado: LancamentoResponseDTO) {
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

    const lancamentoEfetivar: NfceEfetivarRequestDTO = {
      tipo: this.tipo,
      descricao: lancamentoValidado.descricao,
      valor: lancamentoValidado.valor,
      contaId: values.conta?.id,
      contaDestinoId: null,
      cartaoCreditoId: this.tipo === TipoLancamento.DESPESA ? values.cartaoCredito?.id : null,
      faturaId: faturaId,
      mesFatura: mesFatura,
      anoFatura: anoFatura,
      categoriaId: values.categoria?.id,
      subcategoriaId: values.subcategoria?.id,
      dataLancamento: values.dataLancamento ? this.formatarData(values.dataLancamento) : '',
      dataVencimento: values.dataVencimento ? this.formatarData(values.dataVencimento) : '',
      dataEfetivacao: values.dataEfetivacao ? this.formatarData(values.dataEfetivacao) : null,
      observacoes: values.observacoes,
      tipoRecorrencia: values.tipoRecorrencia,
      totalParcelas: values.tipoRecorrencia === TipoRecorrencia.PARCELADO ? values.totalParcelas : null,
      status: values.status,
      valorBruto: (lancamentoValidado as any).valorBruto,
      valorDesconto: (lancamentoValidado as any).valorDesconto,
      chaveNfce: (lancamentoValidado as any).chaveNfce,
      estabelecimento: lancamentoValidado.estabelecimento as any,
      itens: (lancamentoValidado as any).itens
    };

    this.lancamentoService.efetivarNfce(lancamentoEfetivar).subscribe({
      next: () => {
        this.snackBar.open('Lançamento e itens salvos com sucesso!', 'Fechar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: err => {
        console.error(err);
        this.snackBar.open('Erro ao salvar NFC-e.', 'Fechar', { duration: 3000 });
      }
    });
  }

  // ==== AÇÕES PADRÃO ====
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

    let requestPayload: any = {
      tipo: this.tipo,
      descricao: values.descricao,
      valor: values.valor,
      contaId: values.conta?.id,
      contaDestinoId: this.tipo === TipoLancamento.TRANSFERENCIA ? values.contaDestino?.id : null,
      cartaoCreditoId: this.tipo === TipoLancamento.DESPESA ? values.cartaoCredito?.id : null,
      faturaId: faturaId,
      mesFatura: mesFatura,
      anoFatura: anoFatura,
      categoriaId: values.categoria?.id,
      subcategoriaId: values.subcategoria?.id,
      dataLancamento: values.dataLancamento ? this.formatarData(values.dataLancamento) : '',
      dataVencimento: values.dataVencimento ? this.formatarData(values.dataVencimento) : '',
      dataEfetivacao: values.dataEfetivacao ? this.formatarData(values.dataEfetivacao) : null,
      observacoes: values.observacoes,
      tipoRecorrencia: values.tipoRecorrencia,
      totalParcelas: values.tipoRecorrencia === TipoRecorrencia.PARCELADO ? values.totalParcelas : null,
      status: values.status
    };

    if (values.entryMethod === 'manual' && values.itens && values.itens.length > 0) {
      requestPayload.itens = values.itens.map((i: any) => ({
        produto: { id: i.produtoId, nome: i.produtoNome },
        quantidade: i.quantidade,
        valorUnitarioBruto: i.valorUnitarioBruto,
        valorTotalBruto: (i.quantidade || 0) * (i.valorUnitarioBruto || 0)
      }));
    }

    const request$: Observable<any> = this.lancamentoAtual?.id 
      ? this.lancamentoService.atualizar(this.lancamentoAtual.id, requestPayload)
      : this.lancamentoService.criar(requestPayload);

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
