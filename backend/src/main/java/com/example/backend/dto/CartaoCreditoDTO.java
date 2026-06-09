package com.example.backend.dto;

import com.example.backend.model.CartaoCredito;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CartaoCreditoDTO {
    private Long id;
    private String nome;
    private BigDecimal limiteTotal;
    private Integer diaFechamento;
    private Integer diaVencimento;
    private ContaDTO contaPadrao;
    private String bandeira;
    private Boolean principal;

    public CartaoCreditoDTO(CartaoCredito cartao) {
        if(cartao != null) {
            this.id = cartao.getId();
            this.nome = cartao.getNome();
            this.limiteTotal = cartao.getLimiteTotal();
            this.diaFechamento = cartao.getDiaFechamento();
            this.diaVencimento = cartao.getDiaVencimento();
            this.contaPadrao = new ContaDTO(cartao.getContaPadrao());
            this.bandeira = cartao.getBandeira();
            this.principal = cartao.getPrincipal();
        }
    }
}
