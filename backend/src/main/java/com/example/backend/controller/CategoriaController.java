package com.example.backend.controller;

import com.example.backend.dto.CategoriaDTO;
import com.example.backend.dto.CategoriaRequestDTO;
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
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    @GetMapping
    public List<CategoriaDTO> listarTodos() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> criar(@Valid @RequestBody CategoriaRequestDTO dto) {
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

        return ResponseEntity.ok(new CategoriaDTO(categoriaSalva));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada.");
        }

        if (lancamentoRepository.existsByCategoriaId(id)) {
            throw new BusinessException("Não é possível excluir: A categoria possui lançamentos vinculados.");
        }

        List<Subcategoria> subcategorias = subcategoriaRepository.findByCategoriaId(id);
        for (Subcategoria sub : subcategorias) {
            if (lancamentoRepository.existsBySubcategoriaId(sub.getId())) {
                throw new BusinessException("Não é possível excluir: Uma subcategoria desta categoria possui lançamentos vinculados.");
            }
        }

        subcategoriaRepository.deleteAll(subcategorias);
        categoriaRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));

        categoria.setNome(dto.getNome());
        Categoria categoriaAtualizada = categoriaRepository.save(categoria);
        return ResponseEntity.ok(new CategoriaDTO(categoriaAtualizada));
    }
}