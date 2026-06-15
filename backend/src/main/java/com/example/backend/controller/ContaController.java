package com.example.backend.controller;

import com.example.backend.dto.ContaRequestDTO;
import com.example.backend.dto.ContaResponseDTO;
import com.example.backend.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ContaResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    public ContaResponseDTO criar(@Valid @RequestBody ContaRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public ContaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ContaRequestDTO dto) {
        return service.atualizar(id, dto);
    }
}
