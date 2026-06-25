package br.minhasfinancas.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExtractedNfceDTO {
    private String establishmentName;
    private String cnpj;
    private String accessKey;
    private LocalDateTime emissionDate;
    private BigDecimal totalValue;
    private BigDecimal discount;
    private List<ExtractedItemDTO> items;
}

