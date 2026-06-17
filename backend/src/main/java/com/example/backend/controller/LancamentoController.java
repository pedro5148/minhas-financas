package com.example.backend.controller;

import com.example.backend.dto.LancamentoRequestDTO;
import com.example.backend.dto.LancamentoResponseDTO;
import com.example.backend.service.LancamentoService;
import com.example.backend.enums.TipoLancamento;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/lancamentos")
@CrossOrigin(origins = "*")
public class LancamentoController {

    private final LancamentoService service;

    public LancamentoController(LancamentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<LancamentoResponseDTO>> listar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) TipoLancamento tipo,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(descricao, tipo, pageable));
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

    @PostMapping("/lote")
    public List<LancamentoResponseDTO> importarLote(@Valid @RequestBody List<LancamentoRequestDTO> lote) {
        return service.importarLote(lote);
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
