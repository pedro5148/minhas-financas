package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.ItemLancamentoResponseDTO;
import br.minhasfinancas.backend.model.ItemLancamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProdutoMapper.class})
public interface ItemLancamentoMapper {
    @Mapping(target = "valorUnitarioLiquido", ignore = true)
    @Mapping(target = "valorTotalLiquido", ignore = true)
    ItemLancamentoResponseDTO toResponseDTO(ItemLancamento entity);
}
