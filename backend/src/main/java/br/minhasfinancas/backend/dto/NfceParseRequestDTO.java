package br.minhasfinancas.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NfceParseRequestDTO {


    @NotBlank(message = "O conteúdo HTML da nota fiscal é obrigatório e não pode ser vazio.")
    private String htmlContent;

    @NotNull(message = "O ID da conta é obrigatório para o lançamento financeiro.")
    private Long contaId;

    @NotNull(message = "O ID da categoria é obrigatório para classificação do gasto.")
    private Long categoriaId;

    private Long subcategoriaId;

    private LocalDate dataPagamento;
}