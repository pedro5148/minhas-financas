package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.ContaRequestDTO;
import br.minhasfinancas.backend.dto.ContaResponseDTO;
import br.minhasfinancas.backend.model.Conta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContaMapper {
    @Mapping(target = "id", ignore = true)
    Conta toEntity(ContaRequestDTO dto);
    ContaResponseDTO toResponseDTO(Conta entity);
}