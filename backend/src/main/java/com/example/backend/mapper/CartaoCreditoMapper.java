package com.example.backend.mapper;

import com.example.backend.dto.CartaoCreditoRequestDTO;
import com.example.backend.dto.CartaoCreditoResponseDTO;
import com.example.backend.model.CartaoCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ContaMapper.class})
public interface CartaoCreditoMapper {
    @Mapping(target = "contaPadrao.id", source = "contaPadraoId")
    @Mapping(target = "id", ignore = true)
    CartaoCredito toEntity(CartaoCreditoRequestDTO dto);
    
    CartaoCreditoResponseDTO toResponseDTO(CartaoCredito entity);
}
