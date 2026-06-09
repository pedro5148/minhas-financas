package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "saldo_inicial", precision = 19, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    @Column(name = "padrao")
    private boolean padrao = false;

    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDate.now();
        }
    }
}
