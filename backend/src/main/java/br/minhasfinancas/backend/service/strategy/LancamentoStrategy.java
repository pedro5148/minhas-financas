package br.minhasfinancas.backend.service.strategy;

import br.minhasfinancas.backend.dto.LancamentoRequestDTO;
import br.minhasfinancas.backend.dto.LancamentoResponseDTO;

import java.util.List;

public interface LancamentoStrategy {
    boolean isApplicable(LancamentoRequestDTO dto);
    List<LancamentoResponseDTO> processar(LancamentoRequestDTO dto);
}
