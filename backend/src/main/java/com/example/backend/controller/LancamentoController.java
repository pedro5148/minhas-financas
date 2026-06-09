package com.example.backend.controller;

import com.example.backend.dto.LancamentoDTO;
import com.example.backend.dto.LancamentoRequestDTO;
import com.example.backend.model.CartaoCredito;
import com.example.backend.model.Categoria;
import com.example.backend.model.Conta;
import com.example.backend.model.Lancamento;
import com.example.backend.model.Subcategoria;
import com.example.backend.repository.CartaoCreditoRepository;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.ContaRepository;
import com.example.backend.repository.LancamentoRepository;
import com.example.backend.repository.SubcategoriaRepository;
import com.example.backend.service.LancamentoService;
import com.example.backend.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LancamentoController {

    private final LancamentoService service;
    private final LancamentoRepository repository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final CartaoCreditoRepository cartaoRepository;

    @GetMapping
    public List<LancamentoDTO> listarTodos() {
        return repository.findAll().stream()
                .map(LancamentoDTO::new)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/mes/{ano}/{mes}")
    public List<LancamentoDTO> listarPorMesAno(@PathVariable int ano, @PathVariable int mes) {
        return repository.findByMesAnoVencimento(mes, ano).stream()
                .map(LancamentoDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/fatura/{faturaId}")
    public List<LancamentoDTO> listarPorFatura(@PathVariable Long faturaId) {
        return repository.findByFaturaId(faturaId).stream()
                .map(LancamentoDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping
    public List<LancamentoDTO> criar(@Valid @RequestBody LancamentoRequestDTO dto) {
        Lancamento lancamento = mapToEntity(dto);
        return service.criarLancamentos(lancamento).stream()
                .map(LancamentoDTO::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public LancamentoDTO atualizar(@PathVariable Long id, @Valid @RequestBody LancamentoRequestDTO dto) {
        Lancamento lancamento = mapToEntity(dto);
        return new LancamentoDTO(service.atualizar(id, lancamento));
    }

    private Lancamento mapToEntity(LancamentoRequestDTO dto) {
        Lancamento l = new Lancamento();
        l.setTipo(dto.getTipo());
        l.setDescricao(dto.getDescricao());
        l.setValor(dto.getValor());

        Conta conta = contaRepository.findById(dto.getContaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
        l.setConta(conta);

        if (dto.getContaDestinoId() != null) {
            Conta contaDestino = contaRepository.findById(dto.getContaDestinoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta destino não encontrada"));
            l.setContaDestino(contaDestino);
        }

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
            l.setCategoria(categoria);
        }

        if (dto.getSubcategoriaId() != null) {
            Subcategoria subcategoria = subcategoriaRepository.findById(dto.getSubcategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subcategoria não encontrada"));
            l.setSubcategoria(subcategoria);
        }

        l.setDataLancamento(dto.getDataLancamento());
        l.setDataVencimento(dto.getDataVencimento());
        l.setDataEfetivacao(dto.getDataEfetivacao());
        l.setStatus(dto.getStatus());
        l.setObservacoes(dto.getObservacoes());
        l.setTipoRecorrencia(dto.getTipoRecorrencia());
        l.setTotalParcelas(dto.getTotalParcelas());

        if (dto.getCartaoCreditoId() != null) {
            CartaoCredito cartao = cartaoRepository.findById(dto.getCartaoCreditoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cartão de crédito não encontrado"));
            l.setCartaoCredito(cartao);
        }

        return l;
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
