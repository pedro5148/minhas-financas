package com.example.backend.repository;

import com.example.backend.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    
    @Query("SELECT l FROM Lancamento l WHERE EXTRACT(MONTH FROM l.dataVencimento) = :mes AND EXTRACT(YEAR FROM l.dataVencimento) = :ano AND l.cartaoCredito IS NULL")
    List<Lancamento> findByMesAnoVencimento(@Param("mes") int mes, @Param("ano") int ano);

    List<Lancamento> findByFaturaId(Long faturaId);

    @EntityGraph(attributePaths = {"conta", "categoria", "contaDestino", "subcategoria", "cartaoCredito"})
    Page<Lancamento> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "categoria", "contaDestino", "subcategoria", "cartaoCredito"})
    Page<Lancamento> findByTipo(com.example.backend.enums.TipoLancamento tipo, Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "categoria", "contaDestino", "subcategoria", "cartaoCredito"})
    Page<Lancamento> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);

    @EntityGraph(attributePaths = {"conta", "categoria", "contaDestino", "subcategoria", "cartaoCredito"})
    Page<Lancamento> findByDescricaoContainingIgnoreCaseAndTipo(String descricao, com.example.backend.enums.TipoLancamento tipo, Pageable pageable);

    @Query("SELECT l FROM Lancamento l WHERE l.dataLancamento >= :startDate AND l.dataLancamento <= :endDate")
    List<Lancamento> findByDataLancamentoBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByCategoriaId(Long categoriaId);
    boolean existsBySubcategoriaId(Long subcategoriaId);
}
