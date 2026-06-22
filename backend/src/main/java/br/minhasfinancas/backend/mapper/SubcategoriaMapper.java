package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.SubcategoriaRequestDTO;
import br.minhasfinancas.backend.dto.SubcategoriaResponseDTO;
import br.minhasfinancas.backend.model.Subcategoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class})
public interface SubcategoriaMapper {
    @Mapping(target = "categoria.id", source = "categoriaId")
    @Mapping(target = "id", ignore = true)
    Subcategoria toEntity(SubcategoriaRequestDTO dto);
    
    SubcategoriaResponseDTO toResponseDTO(Subcategoria entity);
}
