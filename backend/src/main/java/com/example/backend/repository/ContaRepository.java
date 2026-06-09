package com.example.backend.repository;

import com.example.backend.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {
    java.util.List<Conta> findByPadraoTrue();
}
