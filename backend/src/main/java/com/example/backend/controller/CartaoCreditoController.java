package com.example.backend.controller;

import com.example.backend.model.CartaoCredito;
import com.example.backend.service.CartaoCreditoService;
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
    public List<CartaoCredito> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    public CartaoCredito criar(@RequestBody CartaoCredito cartao) {
        return service.criar(cartao);
    }

    @PutMapping("/{id}")
    public CartaoCredito atualizar(@PathVariable Long id, @RequestBody CartaoCredito cartao) {
        return service.atualizar(id, cartao);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
