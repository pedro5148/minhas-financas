package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.FaturaRequestDTO;
import br.minhasfinancas.backend.dto.FaturaResponseDTO;
import br.minhasfinancas.backend.model.Fatura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartaoCreditoMapper.class})
public interface FaturaMapper {
    @Mapping(target = "cartao.id", source = "cartaoId")
    @Mapping(target = "id", ignore = true)
    Fatura toEntity(FaturaRequestDTO dto);
    
    FaturaResponseDTO toResponseDTO(Fatura entity);
}
