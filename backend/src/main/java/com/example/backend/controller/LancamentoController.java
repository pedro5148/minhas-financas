package com.example.backend.controller;

import com.example.backend.dto.LancamentoRequestDTO;
import com.example.backend.dto.LancamentoResponseDTO;
import com.example.backend.service.LancamentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {

    private final LancamentoService service;

    public LancamentoController(LancamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<LancamentoResponseDTO> listarTodos() {
        return service.listarTodos();
    }
    
    @GetMapping("/mes/{ano}/{mes}")
    public List<LancamentoResponseDTO> listarPorMesAno(@PathVariable int ano, @PathVariable int mes) {
        return service.listarPorMesAno(mes, ano);
    }

    @GetMapping("/fatura/{faturaId}")
    public List<LancamentoResponseDTO> listarPorFatura(@PathVariable Long faturaId) {
        return service.listarPorFatura(faturaId);
    }

    @PostMapping
    public List<LancamentoResponseDTO> criar(@Valid @RequestBody LancamentoRequestDTO dto) {
        return service.criarLancamentos(dto);
    }

    @PutMapping("/{id}")
    public LancamentoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody LancamentoRequestDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
