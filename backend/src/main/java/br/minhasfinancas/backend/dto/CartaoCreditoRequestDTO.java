package br.minhasfinancas.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartaoCreditoRequestDTO {
    @NotBlank(message = "O nome do cartão é obrigatório")
    private String nome;
    
    @NotNull(message = "O limite total é obrigatório")
    private BigDecimal limiteTotal;
    
    @NotNull(message = "O dia de fechamento é obrigatório")
    private Integer diaFechamento;
    
    @NotNull(message = "O dia de vencimento é obrigatório")
    private Integer diaVencimento;
    
    @NotNull(message = "A conta padrão é obrigatória")
    private Long contaPadraoId;
    
    private String bandeira;
    private Boolean principal;
}
