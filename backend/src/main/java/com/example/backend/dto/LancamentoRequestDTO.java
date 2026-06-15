package com.example.backend.dto;

import com.example.backend.enums.StatusLancamento;
import com.example.backend.enums.TipoLancamento;
import com.example.backend.enums.TipoRecorrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LancamentoRequestDTO {
    @NotNull(message = "O tipo de lançamento é obrigatório")
    private TipoLancamento tipo;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    private BigDecimal valor;

    @NotNull(message = "A conta é obrigatória")
    private Long contaId;

    private Long contaDestinoId;
    private Long categoriaId;
    private Long subcategoriaId;

    @NotNull(message = "A data de lançamento é obrigatória")
    private LocalDate dataLancamento;

    @NotNull(message = "A data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    private LocalDate dataEfetivacao;

    @NotNull(message = "O status é obrigatório")
    private StatusLancamento status;

    private String observacoes;

    @NotNull(message = "O tipo de recorrência é obrigatório")
    private TipoRecorrencia tipoRecorrencia;

    private Integer parcelaAtual;
    private Integer totalParcelas;
    private Long cartaoCreditoId;
    private Long faturaId;
}
