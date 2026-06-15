package com.example.backend.mapper;

import com.example.backend.dto.SubcategoriaRequestDTO;
import com.example.backend.dto.SubcategoriaResponseDTO;
import com.example.backend.model.Subcategoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoriaMapper.class})
public interface SubcategoriaMapper {
    @Mapping(target = "categoria.id", source = "categoriaId")
    Subcategoria toEntity(SubcategoriaRequestDTO dto);
    
    SubcategoriaResponseDTO toResponseDTO(Subcategoria entity);
}
