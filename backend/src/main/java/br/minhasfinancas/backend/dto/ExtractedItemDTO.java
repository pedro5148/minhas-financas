package br.minhasfinancas.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExtractedItemDTO {
    private String name;
    private String code;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
