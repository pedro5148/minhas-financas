package br.minhasfinancas.backend.repository;

import br.minhasfinancas.backend.model.Lancamento;
import br.minhasfinancas.backend.enums.TipoLancamento;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class LancamentoSpecification {

    public static Specification<Lancamento> filtroAvancado(String descricao, TipoLancamento tipo, Integer mes, Integer ano) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (descricao != null && !descricao.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("descricao")), "%" + descricao.trim().toLowerCase() + "%"));
            }

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            if (mes != null && ano != null) {
                YearMonth ym = YearMonth.of(ano, mes);
                LocalDate start = ym.atDay(1);
                LocalDate end = ym.atEndOfMonth();
                predicates.add(cb.between(root.get("dataLancamento"), start, end));
            } else if (ano != null) {
                LocalDate start = LocalDate.of(ano, 1, 1);
                LocalDate end = LocalDate.of(ano, 12, 31);
                predicates.add(cb.between(root.get("dataLancamento"), start, end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
