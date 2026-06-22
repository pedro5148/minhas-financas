package br.minhasfinancas.backend.controller;

import br.minhasfinancas.backend.dto.FaturaResponseDTO;
import br.minhasfinancas.backend.service.FaturaService;
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
    public List<FaturaResponseDTO> buscarPorCartao(@PathVariable Long cartaoId) {
        return service.buscarPorCartao(cartaoId);
    }

    @GetMapping("/cartao/{cartaoId}/proximas")
    public List<FaturaResponseDTO> projetarProximasFaturas(@PathVariable Long cartaoId) {
        return service.projetarProximasFaturas(cartaoId);
    }

    @PostMapping("/{id}/pagar")
    public FaturaResponseDTO pagarFatura(@PathVariable Long id) {
        return service.pagarFatura(id);
    }
}
