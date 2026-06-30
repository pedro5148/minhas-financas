package br.minhasfinancas.backend.service.strategy;

import br.minhasfinancas.backend.dto.LancamentoRequestDTO;
import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.mapper.LancamentoMapper;
import br.minhasfinancas.backend.model.Fatura;
import br.minhasfinancas.backend.model.Lancamento;
import br.minhasfinancas.backend.repository.CategoriaRepository;
import br.minhasfinancas.backend.repository.ContaRepository;
import br.minhasfinancas.backend.repository.LancamentoRepository;
import br.minhasfinancas.backend.repository.SubcategoriaRepository;
import br.minhasfinancas.backend.service.FaturaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LancamentoCartaoStrategy extends AbstractLancamentoStrategy {

    private final FaturaService faturaService;

    public LancamentoCartaoStrategy(LancamentoRepository repository,
                                    ContaRepository contaRepository,
                                    CategoriaRepository categoriaRepository,
                                    SubcategoriaRepository subcategoriaRepository,
                                    LancamentoMapper mapper,
                                    FaturaService faturaService) {
        super(repository, contaRepository, categoriaRepository, subcategoriaRepository, mapper);
        this.faturaService = faturaService;
    }

    @Override
    public boolean isApplicable(LancamentoRequestDTO dto) {
        // Aplica-se a despesas com cartão de crédito (cartaoCreditoId preenchido)
        return dto.getCartaoCreditoId() != null && dto.getTipo() == TipoLancamento.DESPESA;
    }

    @Override
    protected void preencherEntidadesEspecificas(Lancamento lancamento, LancamentoRequestDTO dto) {
        if (dto.getFaturaId() != null) {
            lancamento.setFatura(faturaService.buscarPorId(dto.getFaturaId()));
        } else if (dto.getCartaoCreditoId() != null && dto.getMesFatura() != null && dto.getAnoFatura() != null) {
            Fatura fatura = faturaService.obterOuCriarFaturaProjetada(dto.getCartaoCreditoId(), dto.getMesFatura(),
                    dto.getAnoFatura());
            lancamento.setFatura(fatura);
        } else if (dto.getCartaoCreditoId() != null && dto.getDataLancamento() != null) {
            Fatura fatura = faturaService.obterOuCriarFatura(dto.getCartaoCreditoId(), dto.getDataLancamento());
            lancamento.setFatura(fatura);
        } else {
            lancamento.setFatura(null);
        }
    }

    @Override
    protected void posProcessar(List<Lancamento> lancamentosSalvos) {
        for (Lancamento salvo : lancamentosSalvos) {
            if (salvo.getFatura() != null) {
                faturaService.atualizarValorFatura(salvo.getFatura().getId());
            }
        }
    }
}
