package com.example.backend.controller;

import com.example.backend.model.Lancamento;
import com.example.backend.service.LancamentoService;
import com.example.backend.repository.LancamentoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {

    private final LancamentoService service;
    private final LancamentoRepository repository;

    public LancamentoController(LancamentoService service, LancamentoRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public List<Lancamento> listarTodos() {
        return repository.findAll();
    }
    
    @GetMapping("/mes/{ano}/{mes}")
    public List<Lancamento> listarPorMesAno(@PathVariable int ano, @PathVariable int mes) {
        return repository.findByMesAnoVencimento(mes, ano);
    }

    @GetMapping("/fatura/{faturaId}")
    public List<Lancamento> listarPorFatura(@PathVariable Long faturaId) {
        return repository.findByFaturaId(faturaId);
    }

    @PostMapping
    public List<Lancamento> criar(@RequestBody Lancamento lancamento) {
        return service.criarLancamentos(lancamento);
    }

    @PutMapping("/{id}")
    public Lancamento atualizar(@PathVariable Long id, @RequestBody Lancamento lancamento) {
        return service.atualizar(id, lancamento);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
