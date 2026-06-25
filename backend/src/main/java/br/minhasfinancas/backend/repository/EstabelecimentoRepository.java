package br.minhasfinancas.backend.repository;

import br.minhasfinancas.backend.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
    Optional<Estabelecimento> findByCnpj(String cnpj);
}