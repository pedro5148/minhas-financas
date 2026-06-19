package com.example.backend.mapper;

import com.example.backend.dto.CategoriaRequestDTO;
import com.example.backend.dto.CategoriaResponseDTO;
import com.example.backend.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    @Mapping(target = "id", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);
    CategoriaResponseDTO toResponseDTO(Categoria entity);
}
