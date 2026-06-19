package com.example.backend.controller;

import com.example.backend.dto.CartaoCreditoRequestDTO;
import com.example.backend.dto.CartaoCreditoResponseDTO;
import com.example.backend.service.CartaoCreditoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartoes")
@CrossOrigin(origins = "*")
public class CartaoCreditoController {

    private final CartaoCreditoService service;

    public CartaoCreditoController(CartaoCreditoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CartaoCreditoResponseDTO> listarTodos(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        return service.listarTodos(mes, ano);
    }

    @PostMapping
    public CartaoCreditoResponseDTO criar(@Valid @RequestBody CartaoCreditoRequestDTO dto) {
        return service.criar(dto);
    }

    @PutMapping("/{id}")
    public CartaoCreditoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody CartaoCreditoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
