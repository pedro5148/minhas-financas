package com.example.backend.service;

import com.example.backend.dto.CartaoCreditoRequestDTO;
import com.example.backend.dto.CartaoCreditoResponseDTO;
import com.example.backend.mapper.CartaoCreditoMapper;
import com.example.backend.model.CartaoCredito;
import com.example.backend.model.Conta;
import com.example.backend.repository.CartaoCreditoRepository;
import com.example.backend.repository.ContaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartaoCreditoService {

    private final CartaoCreditoRepository repository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoMapper mapper;

    public CartaoCreditoService(CartaoCreditoRepository repository, ContaRepository contaRepository, CartaoCreditoMapper mapper) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.mapper = mapper;
    }

    public List<CartaoCreditoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartaoCreditoResponseDTO criar(CartaoCreditoRequestDTO dto) {
        Conta conta = contaRepository.findById(dto.getContaPadraoId())
                .orElseThrow(() -> new EntityNotFoundException("Conta Padrão não encontrada"));
        CartaoCredito cartao = mapper.toEntity(dto);
        cartao.setContaPadrao(conta);
        return mapper.toResponseDTO(repository.save(cartao));
    }

    @Transactional
    public CartaoCreditoResponseDTO atualizar(Long id, CartaoCreditoRequestDTO dto) {
        return repository.findById(id).map(cartao -> {
            cartao.setNome(dto.getNome());
            cartao.setLimiteTotal(dto.getLimiteTotal());
            cartao.setDiaFechamento(dto.getDiaFechamento());
            cartao.setDiaVencimento(dto.getDiaVencimento());
            cartao.setBandeira(dto.getBandeira());
            cartao.setPrincipal(dto.getPrincipal());
            if (dto.getContaPadraoId() != null) {
                Conta conta = contaRepository.findById(dto.getContaPadraoId())
                    .orElseThrow(() -> new EntityNotFoundException("Conta Padrão não encontrada"));
                cartao.setContaPadrao(conta);
            }
            return mapper.toResponseDTO(repository.save(cartao));
        }).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Cartão não encontrado");
        }
        repository.deleteById(id);
    }
}
