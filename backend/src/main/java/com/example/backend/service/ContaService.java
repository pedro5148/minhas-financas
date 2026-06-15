package com.example.backend.service;

import com.example.backend.dto.ContaRequestDTO;
import com.example.backend.dto.ContaResponseDTO;
import com.example.backend.mapper.ContaMapper;
import com.example.backend.model.Conta;
import com.example.backend.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContaService {

    private final ContaRepository repository;
    private final ContaMapper mapper;

    public ContaService(ContaRepository repository, ContaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ContaResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContaResponseDTO criar(ContaRequestDTO dto) {
        Conta conta = mapper.toEntity(dto);
        if (conta.isPadrao()) {
            resetOutrasContasPadrao(null);
        }
        return mapper.toResponseDTO(repository.save(conta));
    }

    @Transactional
    public ContaResponseDTO atualizar(Long id, ContaRequestDTO dto) {
        return repository.findById(id)
                .map(conta -> {
                    conta.setNome(dto.getNome());
                    conta.setSaldoInicial(dto.getSaldoInicial());
                    conta.setPadrao(dto.isPadrao());
                    if (conta.isPadrao()) {
                        resetOutrasContasPadrao(conta.getId());
                    }
                    return mapper.toResponseDTO(repository.save(conta));
                })
                .orElseGet(() -> {
                    Conta nova = mapper.toEntity(dto);
                    nova.setId(id);
                    if (nova.isPadrao()) {
                        resetOutrasContasPadrao(id);
                    }
                    return mapper.toResponseDTO(repository.save(nova));
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
