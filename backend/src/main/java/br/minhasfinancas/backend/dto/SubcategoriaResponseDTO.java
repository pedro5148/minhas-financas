package br.minhasfinancas.backend.dto;

import lombok.Data;

@Data
public class SubcategoriaResponseDTO {
    private Long id;
    private String nome;
    private CategoriaResponseDTO categoria;
}
