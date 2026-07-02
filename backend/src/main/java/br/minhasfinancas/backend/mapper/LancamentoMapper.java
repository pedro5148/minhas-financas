package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.LancamentoRequestDTO;
import br.minhasfinancas.backend.dto.LancamentoResponseDTO;
import br.minhasfinancas.backend.model.Lancamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        ContaMapper.class,
        CategoriaMapper.class,
        SubcategoriaMapper.class,
        CartaoCreditoMapper.class,
        FaturaMapper.class,
        EstabelecimentoMapper.class,
        ItemLancamentoMapper.class
})
public interface LancamentoMapper {
    @Mapping(target = "conta.id", source = "contaId")
    @Mapping(target = "contaDestino.id", source = "contaDestinoId")
    @Mapping(target = "categoria.id", source = "categoriaId")
    @Mapping(target = "subcategoria.id", source = "subcategoriaId")
    @Mapping(target = "fatura.id", source = "faturaId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chaveNfce", ignore = true)
    @Mapping(target = "lancamentoParcelado", ignore = true)
    @Mapping(target = "valorBruto", ignore = true)
    @Mapping(target = "valorDesconto", ignore = true)
    @Mapping(target = "estabelecimento", ignore = true)
    @Mapping(target = "itens", ignore = true)
    Lancamento toEntity(LancamentoRequestDTO dto);

    @Mapping(target = "cartaoCredito", source = "fatura.cartao")
    @Mapping(target = "nomeCartao", source = "fatura.cartao.nome")
    @Mapping(target = "valorBruto", source = "valorBruto")
    @Mapping(target = "valorDesconto", source = "valorDesconto")
    LancamentoResponseDTO toResponseDTO(Lancamento entity);
}
