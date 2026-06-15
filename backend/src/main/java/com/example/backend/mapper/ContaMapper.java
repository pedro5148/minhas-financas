package com.example.backend.mapper;

import com.example.backend.dto.ContaRequestDTO;
import com.example.backend.dto.ContaResponseDTO;
import com.example.backend.model.Conta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContaMapper {
    Conta toEntity(ContaRequestDTO dto);
    ContaResponseDTO toResponseDTO(Conta entity);
}
