package com.example.backend.mapper;

import com.example.backend.dto.CategoriaRequestDTO;
import com.example.backend.dto.CategoriaResponseDTO;
import com.example.backend.model.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    Categoria toEntity(CategoriaRequestDTO dto);
    CategoriaResponseDTO toResponseDTO(Categoria entity);
}
