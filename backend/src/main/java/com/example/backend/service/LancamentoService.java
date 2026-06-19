package com.example.backend.service;

import com.example.backend.dto.LancamentoRequestDTO;
import com.example.backend.dto.LancamentoResponseDTO;
import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoRecorrencia;
import com.example.backend.mapper.LancamentoMapper;
import com.example.backend.model.Fatura;
import com.example.backend.model.Lancamento;
import com.example.backend.repository.*;
import com.example.backend.enums.TipoLancamento;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class LancamentoService {

    private final LancamentoRepository repository;
    private final FaturaService faturaService;
    private final ContaRepository contaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final LancamentoMapper mapper;
    
    public LancamentoService(LancamentoRepository repository, FaturaService faturaService,
                             ContaRepository contaRepository, SubcategoriaRepository subcategoriaRepository, 
                             CartaoCreditoRepository cartaoRepository, LancamentoMapper mapper) {
        this.repository = repository;
        this.faturaService = faturaService;
        this.contaRepository = contaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.cartaoRepository = cartaoRepository;
        this.mapper = mapper;
    }

    public Page<LancamentoResponseDTO> listar(String descricao, TipoLancamento tipo, Integer mes, Integer ano, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Lancamento> spec = LancamentoSpecification.filtroAvancado(descricao, tipo, mes, ano);
        Page<Lancamento> paginasEntidade = repository.findAll(spec, pageable);
        return paginasEntidade.map(mapper::toResponseDTO);
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
            if (salvo.getFatura() != null) {
                faturaService.atualizarValorFatura(salvo.getFatura().getId());
            }
        }

        return lancamentosSalvos.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<LancamentoResponseDTO> importarLote(List<LancamentoRequestDTO> lote) {
        if (lote == null || lote.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDate minDate = lote.stream().map(LancamentoRequestDTO::getDataLancamento).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = lote.stream().map(LancamentoRequestDTO::getDataLancamento).max(LocalDate::compareTo).orElse(LocalDate.now());

        // Carrega apenas o intervalo de datas do CSV (performance otimizada, previne N+1)
        List<Lancamento> existentes = repository.findByDataLancamentoBetween(minDate, maxDate);
        
        // Gera assinaturas (hash lógico) para validação de idempotência
        Set<String> assinaturasExistentes = existentes.stream()
                .map(this::gerarAssinatura)
                .collect(Collectors.toSet());

        List<Lancamento> lancamentosParaSalvar = new ArrayList<>();

        for (LancamentoRequestDTO dto : lote) {
            String assinaturaDto = gerarAssinatura(dto);
            // Ignora silenciosamente registros que já foram importados
            if (!assinaturasExistentes.contains(assinaturaDto)) {
                Lancamento entidade = preencherEntidadesRelacionadas(mapper.toEntity(dto), dto);
                
                entidade.setParcelaAtual(1);
                entidade.setTotalParcelas(1);
                
                lancamentosParaSalvar.add(entidade);
                // Adiciona a assinatura ao conjunto para evitar duplicados dentro do próprio CSV
                assinaturasExistentes.add(assinaturaDto); 
            }
        }

        // Utiliza Batch Insert nativo (muito mais rápido do que loops .save())
        List<Lancamento> salvos = repository.saveAll(lancamentosParaSalvar);

        // Vincula faturas aos lançamentos de cartão importados
        for (Lancamento salvo : salvos) {
            if (salvo.getFatura() != null) {
                faturaService.atualizarValorFatura(salvo.getFatura().getId());
            }
        }

        return salvos.stream().map(mapper::toResponseDTO).collect(Collectors.toList());
    }

    private String gerarAssinatura(Lancamento l) {
        String contaId = l.getConta() != null ? String.valueOf(l.getConta().getId()) : "null";
        String valorStr = l.getValor() != null ? l.getValor().setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
        return contaId + "_" + l.getDataLancamento() + "_" + valorStr;
    }

    private String gerarAssinatura(LancamentoRequestDTO dto) {
        String contaId = dto.getContaId() != null ? String.valueOf(dto.getContaId()) : "null";
        String valorStr = dto.getValor() != null ? dto.getValor().setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
        return contaId + "_" + dto.getDataLancamento() + "_" + valorStr;
    }

    @Transactional
    public LancamentoResponseDTO atualizar(Long id, LancamentoRequestDTO dto) {
        Lancamento existente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado"));

        Lancamento lancamentoAtualizado = preencherEntidadesRelacionadas(mapper.toEntity(dto), dto);
        Fatura faturaAntiga = existente.getFatura();

        existente.setValor(lancamentoAtualizado.getValor());
        existente.setDescricao(lancamentoAtualizado.getDescricao());
        existente.setSubcategoria(lancamentoAtualizado.getSubcategoria());
        existente.setConta(lancamentoAtualizado.getConta());
        existente.setContaDestino(lancamentoAtualizado.getContaDestino());
        existente.setDataLancamento(lancamentoAtualizado.getDataLancamento());
        existente.setDataVencimento(lancamentoAtualizado.getDataVencimento());
        existente.setDataEfetivacao(lancamentoAtualizado.getDataEfetivacao());
        existente.setStatus(lancamentoAtualizado.getStatus());
        existente.setObservacoes(lancamentoAtualizado.getObservacoes());
        existente.setFatura(lancamentoAtualizado.getFatura());

        Lancamento salvo = repository.save(existente);
        repository.flush();

        // Atualiza o total da fatura nova (ou mantida)
        if (salvo.getFatura() != null) {
            faturaService.atualizarValorFatura(salvo.getFatura().getId());
        }

        // Se a fatura mudou ou foi removida, precisamos limpar a fatura antiga
        if (faturaAntiga != null && (salvo.getFatura() == null || !faturaAntiga.getId().equals(salvo.getFatura().getId()))) {
            long count = repository.countByFaturaId(faturaAntiga.getId());
            if (count == 0) {
                faturaService.excluirFatura(faturaAntiga.getId());
            } else {
                faturaService.atualizarValorFatura(faturaAntiga.getId());
            }
        }

        return mapper.toResponseDTO(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        Lancamento existente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado para exclusão"));
        
        Fatura fatura = existente.getFatura();
        repository.delete(existente);
        repository.flush();
        
        if (fatura != null) {
            long count = repository.countByFaturaId(fatura.getId());
            if (count == 0) {
                faturaService.excluirFatura(fatura.getId());
            } else {
                faturaService.atualizarValorFatura(fatura.getId());
            }
        }
    }

    private Lancamento preencherEntidadesRelacionadas(Lancamento lancamento, LancamentoRequestDTO dto) {
        if (dto.getContaId() != null) {
            lancamento.setConta(contaRepository.findById(dto.getContaId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada")));
        } else {
            lancamento.setConta(null);
        }
        if (dto.getContaDestinoId() != null) {
            lancamento.setContaDestino(contaRepository.findById(dto.getContaDestinoId()).orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada")));
        } else {
            lancamento.setContaDestino(null);
        }
        if (dto.getSubcategoriaId() != null) {
            lancamento.setSubcategoria(subcategoriaRepository.findById(dto.getSubcategoriaId()).orElseThrow(() -> new EntityNotFoundException("Subcategoria não encontrada")));
        } else {
            lancamento.setSubcategoria(null);
        }
        
        if (dto.getFaturaId() != null) {
            lancamento.setFatura(faturaService.buscarPorId(dto.getFaturaId()));
        } else if (dto.getCartaoCreditoId() != null && dto.getMesFatura() != null && dto.getAnoFatura() != null) {
            Fatura fatura = faturaService.obterOuCriarFaturaProjetada(dto.getCartaoCreditoId(), dto.getMesFatura(), dto.getAnoFatura());
            lancamento.setFatura(fatura);
        } else if (dto.getCartaoCreditoId() != null && dto.getDataLancamento() != null) {
            Fatura fatura = faturaService.obterOuCriarFatura(dto.getCartaoCreditoId(), dto.getDataLancamento());
            lancamento.setFatura(fatura);
        } else {
            lancamento.setFatura(null);
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
