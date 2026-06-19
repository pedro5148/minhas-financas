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
    @Mapping(target = "subcategoria.id", source = "subcategoriaId")
    @Mapping(target = "fatura.id", source = "faturaId")
    @Mapping(target = "id", ignore = true)
    Lancamento toEntity(LancamentoRequestDTO dto);
    
    @Mapping(target = "categoria", source = "subcategoria.categoria")
    @Mapping(target = "cartaoCredito", source = "fatura.cartao")
    @Mapping(target = "nomeCartao", source = "fatura.cartao.nome")
    LancamentoResponseDTO toResponseDTO(Lancamento entity);
}
