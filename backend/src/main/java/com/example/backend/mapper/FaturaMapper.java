package com.example.backend.mapper;

import com.example.backend.dto.FaturaRequestDTO;
import com.example.backend.dto.FaturaResponseDTO;
import com.example.backend.model.Fatura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartaoCreditoMapper.class})
public interface FaturaMapper {
    @Mapping(target = "cartao.id", source = "cartaoId")
    @Mapping(target = "id", ignore = true)
    Fatura toEntity(FaturaRequestDTO dto);
    
    FaturaResponseDTO toResponseDTO(Fatura entity);
}
