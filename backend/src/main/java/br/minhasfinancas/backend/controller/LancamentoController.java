package br.minhasfinancas.backend.controller;

import br.minhasfinancas.backend.dto.LancamentoRequestDTO;
import br.minhasfinancas.backend.dto.LancamentoResponseDTO;
import br.minhasfinancas.backend.service.LancamentoService;
import br.minhasfinancas.backend.enums.TipoLancamento;
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
    private final br.minhasfinancas.backend.service.ProcessadorNfceService processadorNfceService;

    public LancamentoController(LancamentoService service, br.minhasfinancas.backend.service.ProcessadorNfceService processadorNfceService) {
        this.service = service;
        this.processadorNfceService = processadorNfceService;
    }

    @GetMapping
    public ResponseEntity<Page<LancamentoResponseDTO>> listar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(descricao, tipo, mes, ano, pageable));
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

    @PostMapping("/nfce/preview")
    public ResponseEntity<LancamentoResponseDTO> previewNfce(
            @Valid @RequestBody br.minhasfinancas.backend.dto.NfceParseRequestDTO dto) {
        return ResponseEntity.ok(processadorNfceService.previewNfce(dto));
    }

    @PostMapping("/nfce/efetivar")
    public ResponseEntity<LancamentoResponseDTO> efetivarNfce(
            @Valid @RequestBody br.minhasfinancas.backend.dto.NfceEfetivarRequestDTO dto) {
        return ResponseEntity.ok(processadorNfceService.efetivarNfce(dto));
    }
}
