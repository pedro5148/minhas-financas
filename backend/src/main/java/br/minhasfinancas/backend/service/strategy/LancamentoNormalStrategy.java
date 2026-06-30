package br.minhasfinancas.backend.service.strategy;

import br.minhasfinancas.backend.dto.LancamentoRequestDTO;
import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.mapper.LancamentoMapper;
import br.minhasfinancas.backend.model.Lancamento;
import br.minhasfinancas.backend.repository.CategoriaRepository;
import br.minhasfinancas.backend.repository.ContaRepository;
import br.minhasfinancas.backend.repository.LancamentoRepository;
import br.minhasfinancas.backend.repository.SubcategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LancamentoNormalStrategy extends AbstractLancamentoStrategy {

    public LancamentoNormalStrategy(LancamentoRepository repository,
            ContaRepository contaRepository,
            CategoriaRepository categoriaRepository,
            SubcategoriaRepository subcategoriaRepository,
            LancamentoMapper mapper) {
        super(repository, contaRepository, categoriaRepository, subcategoriaRepository, mapper);
    }

    @Override
    public boolean isApplicable(LancamentoRequestDTO dto) {
        return dto.getCartaoCreditoId() == null &&
                (dto.getTipo() == TipoLancamento.DESPESA ||
                        dto.getTipo() == TipoLancamento.RECEITA ||
                        dto.getTipo() == TipoLancamento.TRANSFERENCIA);
    }

    @Override
    protected void preencherEntidadesEspecificas(Lancamento lancamento, LancamentoRequestDTO dto) {
        lancamento.setFatura(null);
    }

    @Override
    protected void posProcessar(List<Lancamento> lancamentosSalvos) {
        // Nenhuma ação pós-processamento necessária para lançamentos normais
    }
}
