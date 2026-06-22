package br.minhasfinancas.backend.repository;

import br.minhasfinancas.backend.enums.TipoLancamento;
import br.minhasfinancas.backend.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, JpaSpecificationExecutor<Lancamento> {

    @Query("SELECT l FROM Lancamento l WHERE l.dataVencimento >= :inicioMes AND l.dataVencimento <= :fimMes")
    List<Lancamento> findByMesAnoVencimentoOtimizado(@Param("inicioMes") LocalDate inicioMes,
            @Param("fimMes") LocalDate fimMes);

    @Query("SELECT l.fatura.cartao.id, SUM(l.valor) FROM Lancamento l " +
            "WHERE l.fatura IS NOT NULL AND l.tipo = :tipoDespesa " +
            "AND (:mes IS NULL OR EXTRACT(MONTH FROM l.dataLancamento) = :mes) " +
            "AND (:ano IS NULL OR EXTRACT(YEAR FROM l.dataLancamento) = :ano) " +
            "GROUP BY l.fatura.cartao.id")
    List<Object[]> findValorFaturaAgrupadoPorCartao(@Param("mes") Integer mes,
            @Param("ano") Integer ano,
            @Param("tipoDespesa") TipoLancamento tipoDespesa);

    List<Lancamento> findByFaturaId(Long faturaId);

    long countByFaturaId(Long faturaId);

    @EntityGraph(attributePaths = { "conta", "contaDestino", "subcategoria", "fatura" })
    Page<Lancamento> findAll(Specification<Lancamento> spec, Pageable pageable);

    @EntityGraph(attributePaths = { "conta", "contaDestino", "subcategoria", "fatura" })
    Page<Lancamento> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "conta", "contaDestino", "subcategoria", "fatura" })
    Page<Lancamento> findByTipo(TipoLancamento tipo, Pageable pageable);

    @EntityGraph(attributePaths = { "conta", "contaDestino", "subcategoria", "fatura" })
    Page<Lancamento> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);

    @EntityGraph(attributePaths = { "conta", "contaDestino", "subcategoria", "fatura" })
    Page<Lancamento> findByDescricaoContainingIgnoreCaseAndTipo(String descricao, TipoLancamento tipo,
            Pageable pageable);

    List<Lancamento> findByDataLancamentoBetween(LocalDate startDate, LocalDate endDate);

    boolean existsBySubcategoriaCategoriaId(Long categoriaId);

    boolean existsBySubcategoriaId(Long subcategoriaId);
}
