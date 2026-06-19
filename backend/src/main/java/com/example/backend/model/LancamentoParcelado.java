package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamentos_parcelados")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoParcelado {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantidadeParcelas;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private LocalDate dataInicio;

}
