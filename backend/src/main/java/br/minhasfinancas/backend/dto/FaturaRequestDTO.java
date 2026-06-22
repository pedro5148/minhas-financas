package br.minhasfinancas.backend.dto;

import br.minhasfinancas.backend.enums.StatusFatura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FaturaRequestDTO {
    @NotNull(message = "O cartão é obrigatório")
    private Long cartaoId;
    
    @NotBlank(message = "O mês/ano é obrigatório")
    private String mesAno;
    
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private StatusFatura status;
    private LocalDate dataVencimento;
    private LocalDate dataFechamento;
}
