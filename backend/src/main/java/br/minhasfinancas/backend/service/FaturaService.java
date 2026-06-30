package br.minhasfinancas.backend.service;

import br.minhasfinancas.backend.dto.FaturaResponseDTO;
import br.minhasfinancas.backend.enums.StatusFatura;
import br.minhasfinancas.backend.enums.StatusLancamento;
import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.enums.TipoRecorrencia;
import br.minhasfinancas.backend.mapper.FaturaMapper;
import br.minhasfinancas.backend.model.CartaoCredito;
import br.minhasfinancas.backend.model.Fatura;
import br.minhasfinancas.backend.model.Lancamento;
import br.minhasfinancas.backend.repository.CartaoCreditoRepository;
import br.minhasfinancas.backend.repository.FaturaRepository;
import br.minhasfinancas.backend.repository.LancamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FaturaService {

    private final FaturaRepository faturaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final FaturaMapper mapper;

    public FaturaService(
            FaturaRepository faturaRepository,
            CartaoCreditoRepository cartaoRepository,
            LancamentoRepository lancamentoRepository,
            FaturaMapper mapper) {
        this.faturaRepository = faturaRepository;
        this.cartaoRepository = cartaoRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.mapper = mapper;
    }

    public List<FaturaResponseDTO> buscarPorCartao(Long cartaoId) {
        return faturaRepository.findByCartaoId(cartaoId).stream()
                .map(f -> mapper.toResponseDTO(f))
                .collect(Collectors.toList());
    }

    public Fatura buscarPorId(Long id) {
        return faturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fatura não encontrada"));
    }

    public List<FaturaResponseDTO> projetarProximasFaturas(Long cartaoId) {
        CartaoCredito cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));

        List<Fatura> faturasExistentes = faturaRepository.findByCartaoId(cartaoId);
        List<FaturaResponseDTO> proximas = new java.util.ArrayList<>();

        LocalDate mesReferencia = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            String mesAno = mesReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            Optional<Fatura> faturaOpt = faturasExistentes.stream()
                    .filter(f -> f.getMesAno().equals(mesAno))
                    .findFirst();

            if (faturaOpt.isPresent()) {
                proximas.add(mapper.toResponseDTO(faturaOpt.get()));
            } else {
                Fatura projetada = new Fatura();
                projetada.setCartao(cartao);
                projetada.setMesAno(mesAno);
                projetada.setStatus(StatusFatura.ABERTA);
                projetada.setValorTotal(BigDecimal.ZERO);
                projetada.setValorPago(BigDecimal.ZERO);
                
                LocalDate dtVencimento = LocalDate.of(mesReferencia.getYear(), mesReferencia.getMonth(), cartao.getDiaVencimento());
                if (cartao.getDiaVencimento() < cartao.getDiaFechamento()) {
                    dtVencimento = dtVencimento.plusMonths(1);
                }
                
                projetada.setDataVencimento(dtVencimento);
                projetada.setDataFechamento(LocalDate.of(mesReferencia.getYear(), mesReferencia.getMonth(), cartao.getDiaFechamento()));
                
                proximas.add(mapper.toResponseDTO(projetada));
            }
            mesReferencia = mesReferencia.plusMonths(1);
        }
        return proximas;
    }

    @Transactional
    public Fatura obterOuCriarFaturaProjetada(Long cartaoId, Integer mes, Integer ano) {
        String mesAno = String.format("%02d/%04d", mes, ano);
        Optional<Fatura> faturaOpt = faturaRepository.findByCartaoIdAndMesAno(cartaoId, mesAno);
        if (faturaOpt.isPresent()) {
            return faturaOpt.get();
        }

        CartaoCredito cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));

        Fatura nova = new Fatura();
        nova.setCartao(cartao);
        nova.setMesAno(mesAno);
        nova.setStatus(StatusFatura.ABERTA);
        nova.setValorTotal(BigDecimal.ZERO);
        nova.setValorPago(BigDecimal.ZERO);
        
        LocalDate dtVencimento = LocalDate.of(ano, mes, cartao.getDiaVencimento());
        if (cartao.getDiaVencimento() < cartao.getDiaFechamento()) {
            dtVencimento = dtVencimento.plusMonths(1);
        }
        
        nova.setDataVencimento(dtVencimento);
        nova.setDataFechamento(LocalDate.of(ano, mes, cartao.getDiaFechamento()));

        return faturaRepository.save(nova);
    }

    @Transactional
    public Fatura obterOuCriarFatura(Long cartaoId, LocalDate dataCompra) {
        CartaoCredito cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));

        LocalDate dataFechamentoAtual = LocalDate.of(dataCompra.getYear(), dataCompra.getMonth(), cartao.getDiaFechamento());
        LocalDate mesReferencia;

        if (dataCompra.isBefore(dataFechamentoAtual)) {
            mesReferencia = dataCompra;
        } else {
            mesReferencia = dataCompra.plusMonths(1);
        }

        String mesAno = mesReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy"));

        Optional<Fatura> faturaOpt = faturaRepository.findByCartaoIdAndMesAno(cartaoId, mesAno);
        if (faturaOpt.isPresent()) {
            return faturaOpt.get();
        }

        Fatura nova = new Fatura();
        nova.setCartao(cartao);
        nova.setMesAno(mesAno);
        nova.setStatus(StatusFatura.ABERTA);
        nova.setValorTotal(BigDecimal.ZERO);
        nova.setValorPago(BigDecimal.ZERO);
        
        LocalDate dtVencimento = LocalDate.of(mesReferencia.getYear(), mesReferencia.getMonth(), cartao.getDiaVencimento());
        if (cartao.getDiaVencimento() < cartao.getDiaFechamento()) {
            dtVencimento = dtVencimento.plusMonths(1);
        }
        
        nova.setDataVencimento(dtVencimento);
        nova.setDataFechamento(LocalDate.of(mesReferencia.getYear(), mesReferencia.getMonth(), cartao.getDiaFechamento()));

        return faturaRepository.save(nova);
    }

    @Transactional
    public void atualizarValorFatura(Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new EntityNotFoundException("Fatura não encontrada"));
        
        List<Lancamento> despesas = lancamentoRepository.findByFaturaId(faturaId);
        BigDecimal total = despesas.stream()
                .map(l -> l.getValor())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
                
        fatura.setValorTotal(total);
        faturaRepository.save(fatura);
    }

    @Transactional
    public void excluirFatura(Long faturaId) {
        faturaRepository.deleteById(faturaId);
    }

    @Transactional
    public FaturaResponseDTO pagarFatura(Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new EntityNotFoundException("Fatura não encontrada"));

        if (fatura.getStatus() == StatusFatura.PAGA) {
            throw new RuntimeException("A fatura já está paga");
        }

        fatura.setStatus(StatusFatura.PAGA);
        fatura.setValorPago(fatura.getValorTotal());
        
        Lancamento pagamento = new Lancamento();
        pagamento.setTipo(TipoLancamento.DESPESA);
        pagamento.setDescricao("Pagamento Fatura " + fatura.getCartao().getNome() + " - " + fatura.getMesAno());
        pagamento.setValor(fatura.getValorTotal());
        pagamento.setConta(fatura.getCartao().getContaPadrao());
        pagamento.setDataLancamento(LocalDate.now());
        pagamento.setDataVencimento(LocalDate.now());
        pagamento.setDataEfetivacao(LocalDate.now());
        pagamento.setStatus(StatusLancamento.EFETIVADO);
        pagamento.setTipoRecorrencia(TipoRecorrencia.NENHUMA);
        pagamento.setParcelaAtual(1);
        pagamento.setTotalParcelas(1);

        lancamentoRepository.save(pagamento);

        return mapper.toResponseDTO(faturaRepository.save(fatura));
    }
}
