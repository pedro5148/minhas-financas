package com.example.backend.dto;

import com.example.backend.model.Subcategoria;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubcategoriaDTO {
    private Long id;
    private String nome;
    private CategoriaDTO categoria;

    public SubcategoriaDTO(Subcategoria subcategoria) {
        if(subcategoria != null) {
            this.id = subcategoria.getId();
            this.nome = subcategoria.getNome();
            this.categoria = new CategoriaDTO(subcategoria.getCategoria());
        }
    }
}
