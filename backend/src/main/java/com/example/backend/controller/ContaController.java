package com.example.backend.controller;

import com.example.backend.dto.ContaDTO;
import com.example.backend.dto.ContaRequestDTO;
import com.example.backend.model.Conta;
import com.example.backend.repository.ContaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ContaController {

    private final ContaRepository repository;

    @GetMapping
    public List<ContaDTO> listarTodos() {
        return repository.findAll().stream()
                .map(ContaDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ContaDTO criar(@Valid @RequestBody ContaRequestDTO dto) {
        Conta conta = new Conta();
        conta.setNome(dto.getNome());
        conta.setSaldoInicial(dto.getSaldoInicial());
        conta.setDataCriacao(dto.getDataCriacao());
        conta.setPadrao(dto.isPadrao());

        if (conta.isPadrao()) {
            resetOutrasContasPadrao(null);
        }
        return new ContaDTO(repository.save(conta));
    }

    @PutMapping("/{id}")
    public ContaDTO atualizar(@PathVariable Long id, @Valid @RequestBody ContaRequestDTO dto) {
        return repository.findById(id)
                .map(conta -> {
                    conta.setNome(dto.getNome());
                    conta.setSaldoInicial(dto.getSaldoInicial());
                    conta.setPadrao(dto.isPadrao());
                    
                    if (conta.isPadrao()) {
                        resetOutrasContasPadrao(conta.getId());
                    }
                    
                    return new ContaDTO(repository.save(conta));
                })
                .orElseGet(() -> {
                    Conta novaConta = new Conta();
                    novaConta.setId(id);
                    novaConta.setNome(dto.getNome());
                    novaConta.setSaldoInicial(dto.getSaldoInicial());
                    novaConta.setDataCriacao(dto.getDataCriacao());
                    novaConta.setPadrao(dto.isPadrao());

                    if (novaConta.isPadrao()) {
                        resetOutrasContasPadrao(id);
                    }
                    return new ContaDTO(repository.save(novaConta));
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
