package br.minhasfinancas.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubcategoriaRequestDTO {
    @NotBlank(message = "O nome da subcategoria é obrigatório")
    private String nome;
    
    @NotNull(message = "A categoria é obrigatória")
    private Long categoriaId;
}
