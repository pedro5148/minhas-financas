package com.example.backend.mapper;

import com.example.backend.dto.LancamentoRequestDTO;
import com.example.backend.dto.LancamentoResponseDTO;
import com.example.backend.model.Lancamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    ContaMapper.class, 
    CategoriaMapper.class, 
    SubcategoriaMapper.class, 
    CartaoCreditoMapper.class, 
    FaturaMapper.class
})
public interface LancamentoMapper {
    @Mapping(target = "conta.id", source = "contaId")
    @Mapping(target = "contaDestino.id", source = "contaDestinoId")
    @Mapping(target = "categoria.id", source = "categoriaId")
    @Mapping(target = "subcategoria.id", source = "subcategoriaId")
    @Mapping(target = "cartaoCredito.id", source = "cartaoCreditoId")
    @Mapping(target = "fatura.id", source = "faturaId")
    Lancamento toEntity(LancamentoRequestDTO dto);
    
    LancamentoResponseDTO toResponseDTO(Lancamento entity);
}
