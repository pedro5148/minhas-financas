package com.example.backend.service;

import com.example.backend.dto.LancamentoRequestDTO;
import com.example.backend.dto.LancamentoResponseDTO;
import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoRecorrencia;
import com.example.backend.mapper.LancamentoMapper;
import com.example.backend.model.Fatura;
import com.example.backend.model.Lancamento;
import com.example.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LancamentoService {

    private final LancamentoRepository repository;
    private final FaturaService faturaService;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final LancamentoMapper mapper;

    public LancamentoService(LancamentoRepository repository, FaturaService faturaService,
                             ContaRepository contaRepository, CategoriaRepository categoriaRepository,
                             SubcategoriaRepository subcategoriaRepository, CartaoCreditoRepository cartaoRepository,
                             LancamentoMapper mapper) {
        this.repository = repository;
        this.faturaService = faturaService;
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.cartaoRepository = cartaoRepository;
        this.mapper = mapper;
    }

    public List<LancamentoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<LancamentoResponseDTO> listarPorMesAno(int mes, int ano) {
        return repository.findByMesAnoVencimento(mes, ano).stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<LancamentoResponseDTO> listarPorFatura(Long faturaId) {
        return repository.findByFaturaId(faturaId).stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<LancamentoResponseDTO> criarLancamentos(LancamentoRequestDTO dto) {
        Lancamento lancamentoBase = preencherEntidadesRelacionadas(mapper.toEntity(dto), dto);
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
            // ATUALIZAÇÃO DA ARQUITETURA: Pré-gerar 12 meses por padrão
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

        for (Lancamento salvo : lancamentosSalvos) {
            if (salvo.getCartaoCredito() != null) {
                Fatura fatura = faturaService.obterOuCriarFatura(salvo.getCartaoCredito().getId(), salvo.getDataLancamento());
                salvo.setFatura(fatura);
                repository.save(salvo);
                faturaService.atualizarValorFatura(fatura.getId());
            }
        }

        return lancamentosSalvos.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public LancamentoResponseDTO atualizar(Long id, LancamentoRequestDTO dto) {
        Lancamento existente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado"));

        Lancamento lancamentoAtualizado = preencherEntidadesRelacionadas(mapper.toEntity(dto), dto);

        existente.setValor(lancamentoAtualizado.getValor());
        existente.setDescricao(lancamentoAtualizado.getDescricao());
        existente.setCategoria(lancamentoAtualizado.getCategoria());
        existente.setSubcategoria(lancamentoAtualizado.getSubcategoria());
        existente.setConta(lancamentoAtualizado.getConta());
        existente.setContaDestino(lancamentoAtualizado.getContaDestino());
        existente.setDataLancamento(lancamentoAtualizado.getDataLancamento());
        existente.setDataVencimento(lancamentoAtualizado.getDataVencimento());
        existente.setDataEfetivacao(lancamentoAtualizado.getDataEfetivacao());
        existente.setStatus(lancamentoAtualizado.getStatus());
        existente.setObservacoes(lancamentoAtualizado.getObservacoes());
        existente.setCartaoCredito(lancamentoAtualizado.getCartaoCredito());

        Lancamento salvo = repository.save(existente);

        if (salvo.getCartaoCredito() != null) {
            Fatura fatura = faturaService.obterOuCriarFatura(salvo.getCartaoCredito().getId(), salvo.getDataLancamento());
            salvo.setFatura(fatura);
            repository.save(salvo);
            faturaService.atualizarValorFatura(fatura.getId());
        }

        if (existente.getFatura() != null && (salvo.getFatura() == null || !existente.getFatura().getId().equals(salvo.getFatura().getId()))) {
            faturaService.atualizarValorFatura(existente.getFatura().getId());
        }

        return mapper.toResponseDTO(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        Lancamento existente = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado para exclusão"));
        Fatura fatura = existente.getFatura();
        
        repository.deleteById(id);
        
        if (fatura != null) {
            faturaService.atualizarValorFatura(fatura.getId());
        }
    }

    private Lancamento preencherEntidadesRelacionadas(Lancamento lancamento, LancamentoRequestDTO dto) {
        if (dto.getContaId() != null) {
            lancamento.setConta(contaRepository.findById(dto.getContaId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada")));
        }
        if (dto.getContaDestinoId() != null) {
            lancamento.setContaDestino(contaRepository.findById(dto.getContaDestinoId()).orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada")));
        }
        if (dto.getCategoriaId() != null) {
            lancamento.setCategoria(categoriaRepository.findById(dto.getCategoriaId()).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada")));
        }
        if (dto.getSubcategoriaId() != null) {
            lancamento.setSubcategoria(subcategoriaRepository.findById(dto.getSubcategoriaId()).orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada")));
        }
        if (dto.getCartaoCreditoId() != null) {
            lancamento.setCartaoCredito(cartaoRepository.findById(dto.getCartaoCreditoId()).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado")));
        }
        return lancamento;
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
        copia.setCartaoCredito(base.getCartaoCredito());
        return copia;
    }
}
