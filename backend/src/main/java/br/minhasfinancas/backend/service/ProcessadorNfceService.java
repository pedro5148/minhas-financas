package br.minhasfinancas.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.minhasfinancas.backend.dto.ExtractedItemDTO;
import br.minhasfinancas.backend.dto.ExtractedNfceDTO;
import br.minhasfinancas.backend.dto.LancamentoResponseDTO;
import br.minhasfinancas.backend.dto.NfceParseRequestDTO;
import br.minhasfinancas.backend.enums.StatusLancamento;
import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.exception.RegraNegocioException;
import br.minhasfinancas.backend.model.Estabelecimento;
import br.minhasfinancas.backend.model.Lancamento;
import br.minhasfinancas.backend.model.Produto;
import br.minhasfinancas.backend.model.ItemLancamento;
import br.minhasfinancas.backend.repository.EstabelecimentoRepository;
import br.minhasfinancas.backend.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessadorNfceService {

    private final NfceParserService parserService;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final ProdutoRepository produtoRepository;
    private final LancamentoService lancamentoService;
    private final br.minhasfinancas.backend.repository.LancamentoRepository lancamentoRepository;

    @Transactional
    public LancamentoResponseDTO processarEfetivarNfce(NfceParseRequestDTO request) {
        log.info("Iniciando processamento de NFC-e para a conta ID: {}", request.getContaId());

        ExtractedNfceDTO nfce = parserService.parseHtml(request.getHtmlContent());

        if (nfce.getAccessKey() != null && !nfce.getAccessKey().isBlank()) {
            if (lancamentoRepository.existsByChaveNfce(nfce.getAccessKey())) {
                log.warn("NFC-e já importada anteriormente. Chave: {}", nfce.getAccessKey());
                throw new RegraNegocioException("Esta Nota Fiscal Eletrônica já foi importada anteriormente.");
            }
        }

        validarIntegridadeMatematica(nfce);

        Estabelecimento estabelecimento = resolverEstabelecimento(nfce.getCnpj(), nfce.getEstablishmentName());
        Lancamento lancamento = new Lancamento();

        lancamento.setConta(lancamentoService.buscarContaPorId(request.getContaId()));
        lancamento.setCategoria(lancamentoService.buscarCategoriaPorId(request.getCategoriaId()));
        
        if (request.getSubcategoriaId() != null) {
            br.minhasfinancas.backend.model.Subcategoria sub = lancamentoService.buscarSubcategoriaPorId(request.getSubcategoriaId());
            if (!sub.getCategoria().getId().equals(request.getCategoriaId())) {
                throw new RegraNegocioException("A Subcategoria informada não pertence à Categoria selecionada.");
            }
            lancamento.setSubcategoria(sub);
        }
        
        LocalDate dataRealizacao = request.getDataPagamento();
        if (dataRealizacao == null) {
            dataRealizacao = nfce.getEmissionDate() != null ? nfce.getEmissionDate().toLocalDate() : LocalDate.now();
        }
        lancamento.setDataLancamento(dataRealizacao);
        lancamento.setDataVencimento(dataRealizacao);
        lancamento.setDataEfetivacao(dataRealizacao);
        
        lancamento.setTipo(TipoLancamento.DESPESA);
        lancamento.setStatus(StatusLancamento.EFETIVADO);

        lancamento.setValorBruto(nfce.getTotalValue().add(nfce.getDiscount()));
        lancamento.setValorDesconto(nfce.getDiscount());
        lancamento.setValor(nfce.getTotalValue());
        
        lancamento.setChaveNfce(nfce.getAccessKey());

        lancamento.setEstabelecimento(estabelecimento);
        lancamento.setDescricao("Compra de Mercado - " + estabelecimento.getNome());

        for (ExtractedItemDTO dtoItem : nfce.getItems()) {
            Produto produto = resolverProduto(dtoItem.getName(), dtoItem.getCode(), dtoItem.getUnit());

            ItemLancamento item = new ItemLancamento();
            item.setProduto(produto);
            item.setQuantidade(dtoItem.getQuantity());
            item.setValorUnitarioBruto(dtoItem.getUnitPrice());
            item.setValorTotalBruto(dtoItem.getTotalPrice());
            lancamento.adicionarItem(item);
        }
        log.info("NFC-e processada. Delegando salvamento financeiro ao LancamentoService.");
        return lancamentoService.salvarLancamentoProcessado(lancamento);
    }

    private void validarIntegridadeMatematica(ExtractedNfceDTO nfce) {
        BigDecimal somaItens = nfce.getItems().stream()
                .map(ExtractedItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEsperadoBruto = nfce.getTotalValue().add(nfce.getDiscount());
        BigDecimal diferenca = somaItens.subtract(totalEsperadoBruto).abs();

        if (diferenca.compareTo(new BigDecimal("0.02")) > 0) {
            log.error("Falha de integridade matemática. Soma: {}, Esperado: {}", somaItens, totalEsperadoBruto);
            throw new RegraNegocioException(
                    "Inconsistência matemática na nota. Os valores lidos não batem com o total.");
        }
    }

    private Estabelecimento resolverEstabelecimento(String cnpj, String nome) {
        return estabelecimentoRepository.findByCnpj(cnpj)
                .orElseGet(() -> {
                    Estabelecimento novo = new Estabelecimento();
                    novo.setCnpj(cnpj);
                    novo.setNome(nome);
                    return estabelecimentoRepository.save(novo);
                });
    }

    private Produto resolverProduto(String nome, String codigo, String unidade) {
        if (codigo != null && !codigo.isEmpty()) {
            Optional<Produto> prodPorCodigo = produtoRepository.findByCodigo(codigo);
            if (prodPorCodigo.isPresent())
                return prodPorCodigo.get();
        }

        return produtoRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> {
                    Produto novo = new Produto();
                    novo.setNome(nome);
                    novo.setCodigo(codigo);
                    novo.setUnidade(unidade);
                    return produtoRepository.save(novo);
                });
    }
}