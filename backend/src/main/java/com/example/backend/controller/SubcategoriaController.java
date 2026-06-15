package com.example.backend.controller;

import com.example.backend.dto.SubcategoriaRequestDTO;
import com.example.backend.dto.SubcategoriaResponseDTO;
import com.example.backend.service.SubcategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategorias")
@CrossOrigin(origins = "*")
public class SubcategoriaController {

    private final SubcategoriaService service;

    public SubcategoriaController(SubcategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<SubcategoriaResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<SubcategoriaResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(service.listarPorCategoria(categoriaId));
    }

    @PostMapping
    public ResponseEntity<SubcategoriaResponseDTO> criar(@Valid @RequestBody SubcategoriaRequestDTO dto) {
        return ResponseEntity.ok(service.criar(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
