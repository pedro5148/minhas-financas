package com.example.backend.controller;

import com.example.backend.dto.FaturaDTO;
import com.example.backend.model.Fatura;
import com.example.backend.service.FaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/faturas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FaturaController {

    private final FaturaService service;

    @GetMapping("/cartao/{cartaoId}")
    public List<FaturaDTO> buscarPorCartao(@PathVariable Long cartaoId) {
        return service.buscarPorCartao(cartaoId).stream()
                .map(FaturaDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/pagar")
    public FaturaDTO pagarFatura(@PathVariable Long id) {
        return new FaturaDTO(service.pagarFatura(id));
    }
}
