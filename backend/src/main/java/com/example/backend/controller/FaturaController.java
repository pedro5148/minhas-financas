package com.example.backend.controller;

import com.example.backend.model.Fatura;
import com.example.backend.service.FaturaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faturas")
@CrossOrigin(origins = "*")
public class FaturaController {

    private final FaturaService service;

    public FaturaController(FaturaService service) {
        this.service = service;
    }

    @GetMapping("/cartao/{cartaoId}")
    public List<Fatura> buscarPorCartao(@PathVariable Long cartaoId) {
        return service.buscarPorCartao(cartaoId);
    }

    @PostMapping("/{id}/pagar")
    public Fatura pagarFatura(@PathVariable Long id) {
        return service.pagarFatura(id);
    }
}
