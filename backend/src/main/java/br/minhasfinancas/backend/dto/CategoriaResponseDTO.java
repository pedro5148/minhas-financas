package br.minhasfinancas.backend.dto;

import lombok.Data;

@Data
public class CategoriaResponseDTO {
    private Long id;
    private String nome;
    private Boolean permiteDetalhamento;
}
