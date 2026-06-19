package com.example.backend.repository;

import com.example.backend.model.Lancamento;
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
import org.springframework.lang.Nullable;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, JpaSpecificationExecutor<Lancamento> {
    
    @Query("SELECT l FROM Lancamento l WHERE EXTRACT(MONTH FROM l.dataVencimento) = :mes AND EXTRACT(YEAR FROM l.dataVencimento) = :ano")
    List<Lancamento> findByMesAnoVencimento(@Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT l.fatura.cartao.id, SUM(l.valor) FROM Lancamento l " +
           "WHERE l.fatura IS NOT NULL AND l.tipo = com.example.backend.enums.TipoLancamento.DESPESA " +
           "AND (:mes IS NULL OR EXTRACT(MONTH FROM l.dataLancamento) = :mes) " +
           "AND (:ano IS NULL OR EXTRACT(YEAR FROM l.dataLancamento) = :ano) " +
           "GROUP BY l.fatura.cartao.id")
    List<Object[]> findValorFaturaAgrupadoPorCartao(@Param("mes") Integer mes, @Param("ano") Integer ano);

    List<Lancamento> findByFaturaId(Long faturaId);
    
    long countByFaturaId(Long faturaId);

    @EntityGraph(attributePaths = {"conta", "contaDestino", "subcategoria", "fatura"})
    Page<Lancamento> findAll(@Nullable Specification<Lancamento> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "contaDestino", "subcategoria", "fatura"})
    Page<Lancamento> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "contaDestino", "subcategoria", "fatura"})
    Page<Lancamento> findByTipo(com.example.backend.enums.TipoLancamento tipo, Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "contaDestino", "subcategoria", "fatura"})
    Page<Lancamento> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "contaDestino", "subcategoria", "fatura"})
    Page<Lancamento> findByDescricaoContainingIgnoreCaseAndTipo(String descricao, com.example.backend.enums.TipoLancamento tipo, Pageable pageable);

    @Query("SELECT l FROM Lancamento l WHERE l.dataLancamento >= :startDate AND l.dataLancamento <= :endDate")
    List<Lancamento> findByDataLancamentoBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Lancamento l WHERE l.subcategoria.categoria.id = :categoriaId")
    boolean existsByCategoriaId(@Param("categoriaId") Long categoriaId);
    
    boolean existsBySubcategoriaId(Long subcategoriaId);
}
