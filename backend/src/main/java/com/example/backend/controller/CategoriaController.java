package com.example.backend.controller;

import com.example.backend.dto.CategoriaRequestDTO;
import com.example.backend.model.Categoria;
import com.example.backend.model.Subcategoria;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.LancamentoRepository;
import com.example.backend.repository.SubcategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    public CategoriaController(CategoriaRepository categoriaRepository,
        SubcategoriaRepository subcategoriaRepository,
        LancamentoRepository lancamentoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    @GetMapping
    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Categoria> criar(@RequestBody CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        
        Categoria categoriaSalva = categoriaRepository.save(categoria);

        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setCategoria(categoriaSalva);
        
        if (dto.getSubcategoriaNome() != null && !dto.getSubcategoriaNome().trim().isEmpty()) {
            subcategoria.setNome(dto.getSubcategoriaNome().trim());
        } else {
            subcategoria.setNome("Outros");
        }
        subcategoriaRepository.save(subcategoria);

        return ResponseEntity.ok(categoriaSalva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (lancamentoRepository.existsByCategoriaId(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não é possível excluir: A categoria possui lançamentos vinculados.");
        }

        List<Subcategoria> subcategorias = subcategoriaRepository.findByCategoriaId(id);
        for (Subcategoria sub : subcategorias) {
            if (lancamentoRepository.existsBySubcategoriaId(sub.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Não é possível excluir: Uma subcategoria desta categoria possui lançamentos vinculados.");
            }
        }

        subcategoriaRepository.deleteAll(subcategorias);
        categoriaRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody CategoriaRequestDTO dto) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setNome(dto.getNome());
            Categoria categoriaAtualizada = categoriaRepository.save(categoria);
            return ResponseEntity.ok(categoriaAtualizada);
        }).orElse(ResponseEntity.notFound().build());
    }
}