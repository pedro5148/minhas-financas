package br.minhasfinancas.backend.dto;

import lombok.Data;

@Data
public class EstabelecimentoResponseDTO {
    private Long id;
    private String cnpj;
    private String nome;
}
