import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { FormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { CsvImportService } from '../../services/csv-import.service';
import { ContaService } from '../../services/conta.service';
import { CategoriaService } from '../../services/categoria.service';
import { LancamentoService } from '../../services/lancamento.service';
import { CartaoCreditoService } from '../../services/cartao-credito.service';

import { Conta } from '../../models/conta.model';
import { Categoria } from '../../models/categoria.model';
import { CartaoCredito } from '../../models/cartao-credito.model';
import { LancamentoRequestDTO, TipoLancamento, StatusLancamento, TipoRecorrencia } from '../../models/lancamento.model';
import { NovaCategoriaRapidaComponent } from '../nova-categoria-rapida/nova-categoria-rapida.component';

@Component({
  selector: 'app-importacao-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    FormsModule,
    MatSnackBarModule,
    MatIconModule,
    MatRadioModule,
    MatCheckboxModule
  ],
  templateUrl: './importacao-modal.component.html',
  styleUrl: './importacao-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImportacaoModalComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ImportacaoModalComponent>);
  private csvImportService = inject(CsvImportService);
  private contaService = inject(ContaService);
  private categoriaService = inject(CategoriaService);
  private lancamentoService = inject(LancamentoService);
  private cartaoCreditoService = inject(CartaoCreditoService);
  private snackBar = inject(MatSnackBar);
  private destroyRef = inject(DestroyRef);
  private dialog = inject(MatDialog);

  // Estados reativos (Signals) para OnPush
  headers = signal<string[]>([]);
  csvData = signal<Record<string, string>[]>([]);
  arquivoCarregado = signal<boolean>(false);
  isCarregando = signal<boolean>(false);
  
  // Entidades do banco
  contas = signal<Conta[]>([]);
  categorias = signal<Categoria[]>([]);
  cartoes = signal<CartaoCredito[]>([]);

  // Dicionários de mapeamento relacional (Nome do CSV -> ID do Banco)
  mapeamentoContas = signal<Record<string, number>>({});
  mapeamentoCategorias = signal<Record<string, number>>({});
  mapeamentoCartoes = signal<Record<string, number>>({});

  // Nomes únicos encontrados no CSV para Entidades
  nomesContaCsv = signal<string[]>([]);
  nomesCategoriaCsv = signal<string[]>([]);
  nomesCartaoCsv = signal<string[]>([]);

  tipoGlobal = signal<TipoLancamento>(TipoLancamento.DESPESA);
  tiposLancamento = TipoLancamento;
  marcarComoEfetivado = signal<boolean>(false); // Nova flag para controlar o status do lançamento

  // Formulário de associação de colunas
  mappingForm: FormGroup = this.fb.group({
    dataLancamento: [null, Validators.required],
    descricao: [null, Validators.required],
    valor: [null, Validators.required],
    conta: [null],
    categoria: [null],
    cartao: [null]
  });

  ngOnInit() {
    this.carregarEntidades();

    // Quando o usuário alterar o mapeamento das colunas de relacionamentos,
    // extrair os valores únicos do CSV para pedir associação.
    this.mappingForm.get('conta')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.extrairValoresRelacionais());
      
    this.mappingForm.get('categoria')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.extrairValoresRelacionais());

    this.mappingForm.get('cartao')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.extrairValoresRelacionais());
  }

  private carregarEntidades() {
    this.contaService.listarTodos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(res => this.contas.set(res));
      
    this.categoriaService.listarCategorias()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(res => this.categorias.set(res));

    this.cartaoCreditoService.listarTodos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(res => this.cartoes.set(res));
  }

  async onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      try {
        const result = await this.csvImportService.parseCsv(file);
        this.headers.set(result.headers);
        this.csvData.set(result.data);
        this.arquivoCarregado.set(true);
        this.autoMapearColunas(result.headers);
        this.extrairValoresRelacionais();
      } catch (error) {
        this.snackBar.open('Erro ao ler o arquivo CSV', 'Fechar', { duration: 3000 });
      }
    }
  }

  private autoMapearColunas(headers: string[]) {
    // Tenta associar nomes comuns de cabeçalhos aos campos obrigatórios
    const map = {
      dataLancamento: headers.find(h => h.toLowerCase().includes('data')),
      descricao: headers.find(h => h.toLowerCase().includes('descri') || h.toLowerCase().includes('histórico')),
      valor: headers.find(h => h.toLowerCase().includes('valor') || h.toLowerCase().includes('quantia')),
      conta: headers.find(h => h.toLowerCase().includes('conta')),
      categoria: headers.find(h => h.toLowerCase().includes('categoria')),
      cartao: headers.find(h => h.toLowerCase().includes('cartão') || h.toLowerCase().includes('cartao'))
    };

    this.mappingForm.patchValue(map);
  }

  private extrairValoresRelacionais() {
    const data = this.csvData();
    const map = this.mappingForm.value;

    if (map.conta) {
      const contasUnicas = Array.from(new Set(data.map(row => row[map.conta]).filter(val => !!val)));
      this.nomesContaCsv.set(contasUnicas as string[]);
    }

    if (map.categoria) {
      const categoriasUnicas = Array.from(new Set(data.map(row => row[map.categoria]).filter(val => !!val)));
      this.nomesCategoriaCsv.set(categoriasUnicas as string[]);
    }

    if (map.cartao) {
      const cartoesUnicos = Array.from(new Set(data.map(row => row[map.cartao]).filter(val => !!val)));
      this.nomesCartaoCsv.set(cartoesUnicos as string[]);
    }
  }

  associarConta(nomeCsv: string, idBanco: number) {
    this.mapeamentoContas.update(map => ({ ...map, [nomeCsv]: idBanco }));
  }

  associarCategoria(nomeCsv: string, idBanco: number) {
    this.mapeamentoCategorias.update(map => ({ ...map, [nomeCsv]: idBanco }));
  }

  associarCartao(nomeCsv: string, idBanco: number) {
    this.mapeamentoCartoes.update(map => ({ ...map, [nomeCsv]: idBanco }));
  }

  onCategoriaSelectionChange(event: any, nomeCsv: string) {
    if (event.value === 'NEW') {
      const dialogRef = this.dialog.open(NovaCategoriaRapidaComponent, {
        width: '400px',
        disableClose: true
      });

      dialogRef.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(novaCat => {
        if (novaCat) {
          // Atualiza a lista de categorias e o mapeamento automático
          this.categorias.update(cats => [...cats, novaCat]);
          this.associarCategoria(nomeCsv, novaCat.id);
        } else {
          // Em caso de cancelamento, remove a seleção 'NEW' definindo para null ou undefined
          this.mapeamentoCategorias.update(map => {
            const novoMap = { ...map };
            delete novoMap[nomeCsv];
            return novoMap;
          });
        }
      });
    } else {
      this.associarCategoria(nomeCsv, event.value);
    }
  }

  processar() {
    if (this.mappingForm.invalid) {
      this.mappingForm.markAllAsTouched();
      this.snackBar.open('Por favor, mapeie todos os campos obrigatórios.', 'Fechar', { duration: 3000 });
      return;
    }

    const map = this.mappingForm.value;
    const data = this.csvData();
    const contasMap = this.mapeamentoContas();
    const categoriasMap = this.mapeamentoCategorias();
    const cartoesMap = this.mapeamentoCartoes();

    const lancamentos: LancamentoRequestDTO[] = data.map(row => {
      const valorOriginal = row[map.valor] || '0';
      // Sanitização de valor: remove apenas moedas e espaços. Converte vírgula para ponto.
      const valorLimpo = valorOriginal.replace(/[R$\s]/g, '');
      const valorSanitizado = parseFloat(valorLimpo.replace(',', '.'));

      // Sanitização de data ignorando a hora (ex: "01/06/2026 16:55" -> "YYYY-MM-DD")
      const dataHoraOriginal = row[map.dataLancamento] || '';
      const dataOriginal = dataHoraOriginal.split(' ')[0];
      const partesData = dataOriginal.split(/[\/\-]/);
      let dataIso = dataOriginal;
      if (partesData.length === 3) {
        // Se for DD/MM/YYYY
        if (partesData[0].length === 2) {
          dataIso = `${partesData[2]}-${partesData[1]}-${partesData[0]}`;
        }
      }

      // Sanitização de tipo
      const tipo = this.tipoGlobal();

      const valorFinal = Math.abs(valorSanitizado);

      // Relacionamentos
      const nomeContaCsv = map.conta ? row[map.conta] : null;
      const contaId = nomeContaCsv ? contasMap[nomeContaCsv] : undefined;

      const nomeCategoriaCsv = map.categoria ? row[map.categoria] : null;
      const categoriaId = nomeCategoriaCsv ? categoriasMap[nomeCategoriaCsv] : undefined;

      const nomeCartaoCsv = map.cartao ? row[map.cartao] : null;
      const cartaoId = nomeCartaoCsv ? cartoesMap[nomeCartaoCsv] : undefined;

      return {
        descricao: row[map.descricao],
        valor: isNaN(valorFinal) ? 0 : valorFinal,
        dataLancamento: dataIso,
        dataVencimento: dataIso, // Assume vencimento = lançamento
        dataEfetivacao: dataIso, // Se está no CSV do banco, foi efetivado
        tipo: tipo,
        status: cartaoId ? (this.marcarComoEfetivado() ? StatusLancamento.EFETIVADO : StatusLancamento.PENDENTE) : StatusLancamento.EFETIVADO,
        tipoRecorrencia: TipoRecorrencia.NENHUMA,
        contaId: contaId,
        categoriaId: categoriaId,
        cartaoCreditoId: cartaoId
      } as LancamentoRequestDTO;
    });

    this.isCarregando.set(true);
    
    this.lancamentoService.importarEmLote(lancamentos)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.lancamentoService.notificarAlteracao();
          this.snackBar.open('Importação concluída com sucesso!', 'Fechar', { duration: 4000 });
          this.dialogRef.close(true);
        },
        error: (err) => {
          console.error('Erro na importação:', err);
          this.snackBar.open('Erro ao processar importação. Verifique os dados.', 'Fechar', { duration: 5000 });
          this.isCarregando.set(false);
        }
      });
  }

  fechar() {
    this.dialogRef.close();
  }
}
