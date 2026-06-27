package br.minhasfinancas.backend.dto;

import br.minhasfinancas.backend.enums.StatusLancamento;
import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.enums.TipoRecorrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class NfceEfetivarRequestDTO {
    @NotNull(message = "O tipo de lançamento é obrigatório")
    private TipoLancamento tipo;

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    private BigDecimal valor;

    @NotNull(message = "A conta é obrigatória")
    private Long contaId;

    @NotNull(message = "A categoria é obrigatória")
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

    private BigDecimal valorBruto;
    private BigDecimal valorDesconto;
    private String chaveNfce;
    
    private EstabelecimentoResponseDTO estabelecimento;
    private List<ItemLancamentoResponseDTO> itens;
}
