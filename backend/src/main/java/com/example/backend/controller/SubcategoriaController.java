package com.example.backend.controller;

import com.example.backend.dto.SubcategoriaRequestDTO;
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
@RequestMapping("/api/subcategorias")
@CrossOrigin(origins = "*")
public class SubcategoriaController {

    private final SubcategoriaRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    public SubcategoriaController(SubcategoriaRepository repository, CategoriaRepository categoriaRepository, LancamentoRepository lancamentoRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    @GetMapping
    public List<Subcategoria> listarTodos() {
        return repository.findAll();
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Subcategoria>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(repository.findByCategoriaId(categoriaId));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody SubcategoriaRequestDTO dto) {
        return categoriaRepository.findById(dto.getCategoriaId())
                .map(categoria -> {
                    Subcategoria subcategoria = new Subcategoria();
                    subcategoria.setNome(dto.getNome());
                    subcategoria.setCategoria(categoria);
                    return ResponseEntity.ok(repository.save(subcategoria));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        if (lancamentoRepository.existsBySubcategoriaId(id)) {
             return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não é possível excluir: A subcategoria possui lançamentos vinculados.");
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
