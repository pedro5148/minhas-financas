package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.CategoriaRequestDTO;
import br.minhasfinancas.backend.dto.CategoriaResponseDTO;
import br.minhasfinancas.backend.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    @Mapping(target = "id", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);
    CategoriaResponseDTO toResponseDTO(Categoria entity);
}
