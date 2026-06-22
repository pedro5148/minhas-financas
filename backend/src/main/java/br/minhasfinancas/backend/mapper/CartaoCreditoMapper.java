package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.CartaoCreditoRequestDTO;
import br.minhasfinancas.backend.dto.CartaoCreditoResponseDTO;
import br.minhasfinancas.backend.model.CartaoCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ContaMapper.class})
public interface CartaoCreditoMapper {
    @Mapping(target = "contaPadrao.id", source = "contaPadraoId")
    @Mapping(target = "id", ignore = true)
    CartaoCredito toEntity(CartaoCreditoRequestDTO dto);
    
    CartaoCreditoResponseDTO toResponseDTO(CartaoCredito entity);
}
