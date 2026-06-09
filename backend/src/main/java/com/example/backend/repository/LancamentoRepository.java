package com.example.backend.repository;

import com.example.backend.model.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    
    @Query("SELECT l FROM Lancamento l WHERE EXTRACT(MONTH FROM l.dataVencimento) = :mes AND EXTRACT(YEAR FROM l.dataVencimento) = :ano AND l.cartaoCredito IS NULL")
    List<Lancamento> findByMesAnoVencimento(@Param("mes") int mes, @Param("ano") int ano);

    List<Lancamento> findByFaturaId(Long faturaId);

    boolean existsByCategoriaId(Long categoriaId);
    boolean existsBySubcategoriaId(Long subcategoriaId);
}
