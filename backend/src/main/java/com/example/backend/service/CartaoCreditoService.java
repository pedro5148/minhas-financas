package com.example.backend.service;

import com.example.backend.model.CartaoCredito;
import com.example.backend.repository.CartaoCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartaoCreditoService {

    private final CartaoCreditoRepository repository;

    public List<CartaoCredito> listarTodos() {
        return repository.findAll();
    }

    public CartaoCredito criar(CartaoCredito cartao) {
        return repository.save(cartao);
    }

    public CartaoCredito atualizar(Long id, CartaoCredito atualizado) {
        return repository.findById(id).map(cartao -> {
            cartao.setNome(atualizado.getNome());
            cartao.setLimiteTotal(atualizado.getLimiteTotal());
            cartao.setDiaFechamento(atualizado.getDiaFechamento());
            cartao.setDiaVencimento(atualizado.getDiaVencimento());
            cartao.setContaPadrao(atualizado.getContaPadrao());
            return repository.save(cartao);
        }).orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
