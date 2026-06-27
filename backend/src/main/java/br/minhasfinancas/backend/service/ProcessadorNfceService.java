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
    private final br.minhasfinancas.backend.mapper.LancamentoMapper lancamentoMapper;

    @Transactional
    public LancamentoResponseDTO previewNfce(NfceParseRequestDTO request) {
        log.info("Iniciando preview de NFC-e para a conta ID: {}", request.getContaId());

        try {
            java.nio.file.Files.writeString(java.nio.file.Paths.get("sefaz_es_debug.html"), request.getHtmlContent());
        } catch (Exception e) {
            log.error("Erro ao salvar HTML de debug", e);
        }

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
        log.info("NFC-e processada em memória para preview.");
        return lancamentoMapper.toResponseDTO(lancamento);
    }

    @Transactional
    public LancamentoResponseDTO efetivarNfce(br.minhasfinancas.backend.dto.NfceEfetivarRequestDTO dto) {
        log.info("Efetivando NFC-e na base de dados...");

        if (dto.getChaveNfce() != null && !dto.getChaveNfce().isBlank()) {
            if (lancamentoRepository.existsByChaveNfce(dto.getChaveNfce())) {
                throw new RegraNegocioException("Esta Nota Fiscal Eletrônica já foi importada anteriormente.");
            }
        }

        Lancamento lancamento = new Lancamento();

        lancamento.setConta(lancamentoService.buscarContaPorId(dto.getContaId()));
        lancamento.setCategoria(lancamentoService.buscarCategoriaPorId(dto.getCategoriaId()));
        if (dto.getSubcategoriaId() != null) {
            lancamento.setSubcategoria(lancamentoService.buscarSubcategoriaPorId(dto.getSubcategoriaId()));
        }

        lancamento.setDataLancamento(dto.getDataLancamento());
        lancamento.setDataVencimento(dto.getDataVencimento());
        lancamento.setDataEfetivacao(dto.getDataEfetivacao());
        lancamento.setTipo(dto.getTipo());
        lancamento.setStatus(dto.getStatus());
        lancamento.setValor(dto.getValor());
        lancamento.setValorBruto(dto.getValorBruto());
        lancamento.setValorDesconto(dto.getValorDesconto());
        lancamento.setChaveNfce(dto.getChaveNfce());
        lancamento.setDescricao(dto.getDescricao());
        lancamento.setObservacoes(dto.getObservacoes());
        lancamento.setTipoRecorrencia(dto.getTipoRecorrencia());
        lancamento.setParcelaAtual(1);
        lancamento.setTotalParcelas(1);

        if (dto.getEstabelecimento() != null) {
            Estabelecimento est = resolverEstabelecimento(dto.getEstabelecimento().getCnpj(), dto.getEstabelecimento().getNome());
            lancamento.setEstabelecimento(est);
        }

        if (dto.getItens() != null) {
            for (br.minhasfinancas.backend.dto.ItemLancamentoResponseDTO dtoItem : dto.getItens()) {
                Produto produto = null;
                if (dtoItem.getProduto() != null) {
                    produto = resolverProduto(dtoItem.getProduto().getNome(), dtoItem.getProduto().getCodigo(), dtoItem.getProduto().getUnidade());
                }

                ItemLancamento item = new ItemLancamento();
                item.setProduto(produto);
                item.setQuantidade(dtoItem.getQuantidade());
                item.setValorUnitarioBruto(dtoItem.getValorUnitarioBruto());
                item.setValorTotalBruto(dtoItem.getValorTotalBruto());
                lancamento.adicionarItem(item);
            }
        }

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