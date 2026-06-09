package com.example.backend.controller;

import com.example.backend.dto.CartaoCreditoDTO;
import com.example.backend.dto.CartaoCreditoRequestDTO;
import com.example.backend.model.CartaoCredito;
import com.example.backend.model.Conta;
import com.example.backend.repository.ContaRepository;
import com.example.backend.service.CartaoCreditoService;
import com.example.backend.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cartoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CartaoCreditoController {

    private final CartaoCreditoService service;

    @GetMapping
    public List<CartaoCreditoDTO> listarTodos() {
        return service.listarTodos().stream()
                .map(CartaoCreditoDTO::new)
                .collect(Collectors.toList());
    }

    private final ContaRepository contaRepository;

    @PostMapping
    public CartaoCreditoDTO criar(@Valid @RequestBody CartaoCreditoRequestDTO dto) {
        CartaoCredito cartao = mapToEntity(dto);
        return new CartaoCreditoDTO(service.criar(cartao));
    }

    @PutMapping("/{id}")
    public CartaoCreditoDTO atualizar(@PathVariable Long id, @Valid @RequestBody CartaoCreditoRequestDTO dto) {
        CartaoCredito cartao = mapToEntity(dto);
        return new CartaoCreditoDTO(service.atualizar(id, cartao));
    }

    private CartaoCredito mapToEntity(CartaoCreditoRequestDTO dto) {
        Conta conta = contaRepository.findById(dto.getContaPadraoId())
            .orElseThrow(() -> new ResourceNotFoundException("Conta Padrão não encontrada"));

        CartaoCredito cartao = new CartaoCredito();
        cartao.setNome(dto.getNome());
        cartao.setLimiteTotal(dto.getLimiteTotal());
        cartao.setDiaFechamento(dto.getDiaFechamento());
        cartao.setDiaVencimento(dto.getDiaVencimento());
        cartao.setContaPadrao(conta);
        cartao.setBandeira(dto.getBandeira());
        cartao.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : false);

        return cartao;
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
