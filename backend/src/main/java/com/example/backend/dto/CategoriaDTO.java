package com.example.backend.dto;

import com.example.backend.model.Categoria;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoriaDTO {
    private Long id;
    private String nome;

    public CategoriaDTO(Categoria categoria) {
        if(categoria != null) {
            this.id = categoria.getId();
            this.nome = categoria.getNome();
        }
    }
}
