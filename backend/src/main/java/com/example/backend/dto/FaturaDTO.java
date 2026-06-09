package com.example.backend.dto;

import com.example.backend.enums.StatusFatura;
import com.example.backend.model.Fatura;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FaturaDTO {
    private Long id;
    private CartaoCreditoDTO cartao;
    private String mesAno;
    private BigDecimal valorTotal;
    private BigDecimal valorPago;
    private StatusFatura status;
    private LocalDate dataVencimento;
    private LocalDate dataFechamento;

    public FaturaDTO(Fatura fatura) {
        if(fatura != null) {
            this.id = fatura.getId();
            this.cartao = new CartaoCreditoDTO(fatura.getCartao());
            this.mesAno = fatura.getMesAno();
            this.valorTotal = fatura.getValorTotal();
            this.valorPago = fatura.getValorPago();
            this.status = fatura.getStatus();
            this.dataVencimento = fatura.getDataVencimento();
            this.dataFechamento = fatura.getDataFechamento();
        }
    }
}
