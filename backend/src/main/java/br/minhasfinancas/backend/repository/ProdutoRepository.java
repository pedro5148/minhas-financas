package br.minhasfinancas.backend.repository;

import br.minhasfinancas.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByCodigo(String codigo);
    Optional<Produto> findByNomeIgnoreCase(String nome);
}