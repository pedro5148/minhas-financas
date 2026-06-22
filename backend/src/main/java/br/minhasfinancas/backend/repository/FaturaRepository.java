package br.minhasfinancas.backend.repository;

import br.minhasfinancas.backend.model.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Long> {
    List<Fatura> findByCartaoId(Long cartaoId);
    Optional<Fatura> findByCartaoIdAndMesAno(Long cartaoId, String mesAno);
    List<Fatura> findByMesAno(String mesAno);
}
