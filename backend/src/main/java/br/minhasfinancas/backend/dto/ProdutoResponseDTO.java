package br.minhasfinancas.backend.dto;

import lombok.Data;

@Data
public class ProdutoResponseDTO {
    private Long id;
    private String codigo;
    private String nome;
    private String unidade;
}
