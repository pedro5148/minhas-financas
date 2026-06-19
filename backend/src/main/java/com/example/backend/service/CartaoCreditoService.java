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
import java.math.BigDecimal;
import java.util.Map;
import com.example.backend.repository.FaturaRepository;
import com.example.backend.model.Fatura;

@Service
public class CartaoCreditoService {

    private final CartaoCreditoRepository repository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoMapper mapper;
    private final FaturaRepository faturaRepository;

    public CartaoCreditoService(CartaoCreditoRepository repository, ContaRepository contaRepository, CartaoCreditoMapper mapper, FaturaRepository faturaRepository) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.mapper = mapper;
        this.faturaRepository = faturaRepository;
    }

    public List<CartaoCreditoResponseDTO> listarTodos(Integer mes, Integer ano) {
        List<CartaoCreditoResponseDTO> cartoes = repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());

        if (mes != null && ano != null) {
            String mesAnoStr = String.format("%02d/%04d", mes, ano);
            List<Fatura> faturas = faturaRepository.findByMesAno(mesAnoStr);
            Map<Long, BigDecimal> valoresPorCartao = faturas.stream()
                    .collect(Collectors.toMap(
                            f -> f.getCartao().getId(),
                            Fatura::getValorTotal,
                            (v1, v2) -> v1 // in case of duplicates, though there shouldn't be
                    ));

            cartoes.forEach(cartao -> {
                cartao.setValorFatura(valoresPorCartao.getOrDefault(cartao.getId(), BigDecimal.ZERO));
            });
        } else {
            cartoes.forEach(cartao -> cartao.setValorFatura(BigDecimal.ZERO));
        }

        return cartoes;
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
