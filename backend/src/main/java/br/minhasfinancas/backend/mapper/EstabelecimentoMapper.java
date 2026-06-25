package br.minhasfinancas.backend.mapper;

import br.minhasfinancas.backend.dto.EstabelecimentoResponseDTO;
import br.minhasfinancas.backend.model.Estabelecimento;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EstabelecimentoMapper {
    EstabelecimentoResponseDTO toResponseDTO(Estabelecimento entity);
}
