package com.example.backend.mapper;

import com.example.backend.dto.ContaRequestDTO;
import com.example.backend.dto.ContaResponseDTO;
import com.example.backend.model.Conta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContaMapper {
    @Mapping(target = "id", ignore = true)
    Conta toEntity(ContaRequestDTO dto);
    ContaResponseDTO toResponseDTO(Conta entity);
}