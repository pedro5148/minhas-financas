package com.example.backend.service;

import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoRecorrencia;
import com.example.backend.model.Fatura;
import com.example.backend.model.Lancamento;
import com.example.backend.repository.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final LancamentoRepository repository;
    private final FaturaService faturaService;

    @Transactional
    public List<Lancamento> criarLancamentos(Lancamento lancamentoBase) {
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
        } else {
            lancamentoBase.setParcelaAtual(1);
            if (lancamentoBase.getTipoRecorrencia() == TipoRecorrencia.NENHUMA) {
                lancamentoBase.setTotalParcelas(1);
            }
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

        return lancamentosSalvos;
    }

    @Transactional
    public Lancamento atualizar(Long id, Lancamento lancamentoAtualizado) {
        Lancamento existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado"));

        boolean eraPendente = existente.getStatus() == StatusLancamento.PENDENTE;
        boolean virouEfetivado = lancamentoAtualizado.getStatus() == StatusLancamento.EFETIVADO;

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

        Lancamento salvo = repository.save(existente);

        if (eraPendente && virouEfetivado && salvo.getTipoRecorrencia() == TipoRecorrencia.MENSAL) {
            Lancamento proximo = copiarLancamento(salvo);
            proximo.setId(null);
            proximo.setStatus(StatusLancamento.PENDENTE);
            proximo.setDataEfetivacao(null);
            
            proximo.setDataLancamento(salvo.getDataLancamento().plusMonths(1));
            proximo.setDataVencimento(salvo.getDataVencimento().plusMonths(1));
            
            repository.save(proximo);
        }

        if (salvo.getCartaoCredito() != null) {
            Fatura fatura = faturaService.obterOuCriarFatura(salvo.getCartaoCredito().getId(), salvo.getDataLancamento());
            salvo.setFatura(fatura);
            repository.save(salvo);
            faturaService.atualizarValorFatura(fatura.getId());
        }

        if (existente.getFatura() != null && (salvo.getFatura() == null || !existente.getFatura().getId().equals(salvo.getFatura().getId()))) {
            faturaService.atualizarValorFatura(existente.getFatura().getId());
        }

        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        Lancamento existente = repository.findById(id).orElseThrow(() -> new RuntimeException("Lançamento não encontrado para exclusão"));
        Fatura fatura = existente.getFatura();
        
        repository.deleteById(id);
        
        if (fatura != null) {
            faturaService.atualizarValorFatura(fatura.getId());
        }
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
        return copia;
    }
}
