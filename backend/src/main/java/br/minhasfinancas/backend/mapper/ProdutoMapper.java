package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.ProdutoResponseDTO;
import br.minhasfinancas.backend.model.Produto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {
    ProdutoResponseDTO toResponseDTO(Produto entity);
}
