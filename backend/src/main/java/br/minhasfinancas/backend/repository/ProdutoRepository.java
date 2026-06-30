package br.minhasfinancas.backend.repository;

import br.minhasfinancas.backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByCodigo(String codigo);

    List<Produto> findByNomeIgnoreCase(String nome);
}