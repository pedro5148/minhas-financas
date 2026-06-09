package com.example.backend.controller;

import com.example.backend.model.Conta;
import com.example.backend.repository.ContaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
public class ContaController {

    private final ContaRepository repository;

    public ContaController(ContaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Conta> listarTodos() {
        return repository.findAll();
    }

    @PostMapping
    public Conta criar(@RequestBody Conta conta) {
        if (conta.isPadrao()) {
            resetOutrasContasPadrao(null);
        }
        return repository.save(conta);
    }

    @PutMapping("/{id}")
    public Conta atualizar(@PathVariable Long id, @RequestBody Conta contaAtualizada) {
        return repository.findById(id)
                .map(conta -> {
                    conta.setNome(contaAtualizada.getNome());
                    conta.setSaldoInicial(contaAtualizada.getSaldoInicial());
                    conta.setPadrao(contaAtualizada.isPadrao());
                    
                    if (conta.isPadrao()) {
                        resetOutrasContasPadrao(conta.getId());
                    }
                    
                    return repository.save(conta);
                })
                .orElseGet(() -> {
                    contaAtualizada.setId(id);
                    if (contaAtualizada.isPadrao()) {
                        resetOutrasContasPadrao(id);
                    }
                    return repository.save(contaAtualizada);
                });
    }

    private void resetOutrasContasPadrao(Long idIgnorar) {
        List<Conta> contasPadrao = repository.findByPadraoTrue();
        for (Conta c : contasPadrao) {
            if (idIgnorar == null || !c.getId().equals(idIgnorar)) {
                c.setPadrao(false);
                repository.save(c);
            }
        }
    }
}
