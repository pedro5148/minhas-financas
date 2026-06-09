package com.example.backend.controller;

import com.example.backend.dto.SubcategoriaDTO;
import com.example.backend.dto.SubcategoriaRequestDTO;
import com.example.backend.model.Categoria;
import com.example.backend.model.Subcategoria;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.LancamentoRepository;
import com.example.backend.repository.SubcategoriaRepository;
import jakarta.validation.Valid;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subcategorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SubcategoriaController {

    private final SubcategoriaRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    @GetMapping
    public List<SubcategoriaDTO> listarTodos() {
        return repository.findAll().stream()
                .map(SubcategoriaDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<SubcategoriaDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        List<SubcategoriaDTO> result = repository.findByCategoriaId(categoriaId).stream()
                .map(SubcategoriaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<SubcategoriaDTO> criar(@Valid @RequestBody SubcategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));

        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setNome(dto.getNome());
        subcategoria.setCategoria(categoria);
        Subcategoria subSalva = repository.save(subcategoria);
        return ResponseEntity.ok(new SubcategoriaDTO(subSalva));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Subcategoria não encontrada.");
        }

        if (lancamentoRepository.existsBySubcategoriaId(id)) {
             throw new BusinessException("Não é possível excluir: A subcategoria possui lançamentos vinculados.");
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
