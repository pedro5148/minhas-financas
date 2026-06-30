package br.minhasfinancas.backend.service.strategy;

import br.minhasfinancas.backend.dto.LancamentoRequestDTO;
import br.minhasfinancas.backend.dto.LancamentoResponseDTO;
import br.minhasfinancas.backend.enums.StatusLancamento;
import br.minhasfinancas.backend.enums.TipoRecorrencia;
import br.minhasfinancas.backend.exception.RegraNegocioException;
import br.minhasfinancas.backend.mapper.LancamentoMapper;
import br.minhasfinancas.backend.model.Lancamento;
import br.minhasfinancas.backend.model.Subcategoria;
import br.minhasfinancas.backend.repository.CategoriaRepository;
import br.minhasfinancas.backend.repository.ContaRepository;
import br.minhasfinancas.backend.repository.LancamentoRepository;
import br.minhasfinancas.backend.repository.SubcategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractLancamentoStrategy implements LancamentoStrategy {

    protected final LancamentoRepository repository;
    protected final ContaRepository contaRepository;
    protected final CategoriaRepository categoriaRepository;
    protected final SubcategoriaRepository subcategoriaRepository;
    protected final LancamentoMapper mapper;

    public AbstractLancamentoStrategy(LancamentoRepository repository,
                                      ContaRepository contaRepository,
                                      CategoriaRepository categoriaRepository,
                                      SubcategoriaRepository subcategoriaRepository,
                                      LancamentoMapper mapper) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public List<LancamentoResponseDTO> processar(LancamentoRequestDTO dto) {
        Lancamento lancamentoBase = mapper.toEntity(dto);
        preencherEntidadesComuns(lancamentoBase, dto);
        preencherEntidadesEspecificas(lancamentoBase, dto);

        List<Lancamento> lancamentosSalvos = gerarESalvarLancamentos(lancamentoBase);
        posProcessar(lancamentosSalvos);

        return lancamentosSalvos.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    protected void preencherEntidadesComuns(Lancamento lancamento, LancamentoRequestDTO dto) {
        if (dto.getContaId() != null) {
            lancamento.setConta(contaRepository.findById(dto.getContaId())
                    .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada")));
        } else {
            lancamento.setConta(null);
        }
        if (dto.getContaDestinoId() != null) {
            lancamento.setContaDestino(contaRepository.findById(dto.getContaDestinoId())
                    .orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada")));
        } else {
            lancamento.setContaDestino(null);
        }

        if (dto.getCategoriaId() != null) {
            lancamento.setCategoria(categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada")));
        } else {
            throw new RegraNegocioException("Categoria é obrigatória.");
        }

        if (dto.getSubcategoriaId() != null) {
            Subcategoria sub = subcategoriaRepository.findById(dto.getSubcategoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada"));

            if (!sub.getCategoria().getId().equals(dto.getCategoriaId())) {
                throw new RegraNegocioException(
                        "A Subcategoria informada não pertence à Categoria selecionada.");
            }
            lancamento.setSubcategoria(sub);
        } else {
            lancamento.setSubcategoria(null);
        }
    }

    protected abstract void preencherEntidadesEspecificas(Lancamento lancamento, LancamentoRequestDTO dto);

    protected abstract void posProcessar(List<Lancamento> lancamentosSalvos);

    protected List<Lancamento> gerarESalvarLancamentos(Lancamento lancamentoBase) {
        List<Lancamento> lancamentosSalvos = new ArrayList<>();

        if (lancamentoBase.getTipoRecorrencia() == TipoRecorrencia.PARCELADO) {
            int parcelas = lancamentoBase.getTotalParcelas() != null ? lancamentoBase.getTotalParcelas() : 1;
            BigDecimal valorParcela = lancamentoBase.getValor().divide(BigDecimal.valueOf(parcelas), RoundingMode.HALF_UP);
            BigDecimal valorTotalCalculado = valorParcela.multiply(BigDecimal.valueOf(parcelas));
            BigDecimal diferenca = lancamentoBase.getValor().subtract(valorTotalCalculado);

            for (int i = 1; i <= parcelas; i++) {
                Lancamento p = copiarLancamento(lancamentoBase);
                p.setParcelaAtual(i);
                if (i == parcelas && diferenca.compareTo(BigDecimal.ZERO) != 0) {
                    p.setValor(valorParcela.add(diferenca));
                } else {
                    p.setValor(valorParcela);
                }

                if (i > 1) {
                    p.setDataLancamento(lancamentoBase.getDataLancamento().plusMonths(i - 1));
                    p.setDataVencimento(lancamentoBase.getDataVencimento().plusMonths(i - 1));
                }
                lancamentosSalvos.add(repository.save(p));
            }
        } else if (lancamentoBase.getTipoRecorrencia() == TipoRecorrencia.MENSAL) {
            int parcelas = 12;
            lancamentoBase.setTotalParcelas(parcelas);
            for (int i = 1; i <= parcelas; i++) {
                Lancamento p = copiarLancamento(lancamentoBase);
                p.setParcelaAtual(i);
                p.setStatus(i == 1 ? lancamentoBase.getStatus() : StatusLancamento.PENDENTE);
                if (i > 1) {
                    p.setDataLancamento(lancamentoBase.getDataLancamento().plusMonths(i - 1));
                    p.setDataVencimento(lancamentoBase.getDataVencimento().plusMonths(i - 1));
                    p.setDataEfetivacao(null);
                }
                lancamentosSalvos.add(repository.save(p));
            }
        } else {
            lancamentoBase.setParcelaAtual(1);
            lancamentoBase.setTotalParcelas(1);
            lancamentosSalvos.add(repository.save(lancamentoBase));
        }
        return lancamentosSalvos;
    }

    private Lancamento copiarLancamento(Lancamento base) {
        Lancamento copia = new Lancamento();
        copia.setTipo(base.getTipo());
        copia.setDescricao(base.getDescricao());
        copia.setValor(base.getValor());
        copia.setConta(base.getConta());
        copia.setContaDestino(base.getContaDestino());
        copia.setCategoria(base.getCategoria());
        copia.setSubcategoria(base.getSubcategoria());
        copia.setDataLancamento(base.getDataLancamento());
        copia.setDataVencimento(base.getDataVencimento());
        copia.setDataEfetivacao(base.getDataEfetivacao());
        copia.setStatus(base.getStatus());
        copia.setObservacoes(base.getObservacoes());
        copia.setTipoRecorrencia(base.getTipoRecorrencia());
        copia.setParcelaAtual(base.getParcelaAtual());
        copia.setTotalParcelas(base.getTotalParcelas());
        copia.setFatura(base.getFatura());
        copia.setLancamentoParcelado(base.getLancamentoParcelado());
        return copia;
    }
}
