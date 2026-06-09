package com.example.backend.service;

import com.example.backend.enums.StatusFatura;
import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoLancamento;
import com.example.backend.enums.TipoRecorrencia;
import com.example.backend.model.CartaoCredito;
import com.example.backend.model.Fatura;
import com.example.backend.model.Lancamento;
import com.example.backend.repository.CartaoCreditoRepository;
import com.example.backend.repository.FaturaRepository;
import com.example.backend.repository.LancamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class FaturaService {

    private final FaturaRepository faturaRepository;
    private final CartaoCreditoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;

    public FaturaService(FaturaRepository faturaRepository, CartaoCreditoRepository cartaoRepository, LancamentoRepository lancamentoRepository) {
        this.faturaRepository = faturaRepository;
        this.cartaoRepository = cartaoRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public List<Fatura> buscarPorCartao(Long cartaoId) {
        return faturaRepository.findByCartaoId(cartaoId);
    }

    @Transactional
    public Fatura obterOuCriarFatura(Long cartaoId, LocalDate dataCompra) {
        CartaoCredito cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

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
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada"));
        
        List<Lancamento> despesas = lancamentoRepository.findByFaturaId(faturaId);
        BigDecimal total = despesas.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        fatura.setValorTotal(total);
        faturaRepository.save(fatura);
    }

    @Transactional
    public Fatura pagarFatura(Long faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada"));

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

        return faturaRepository.save(fatura);
    }
}
