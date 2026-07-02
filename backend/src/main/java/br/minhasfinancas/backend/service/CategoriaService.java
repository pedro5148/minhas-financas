package br.minhasfinancas.backend.service;

import br.minhasfinancas.backend.dto.CategoriaRequestDTO;
import br.minhasfinancas.backend.dto.CategoriaResponseDTO;
import br.minhasfinancas.backend.mapper.CategoriaMapper;
import br.minhasfinancas.backend.model.Categoria;
import br.minhasfinancas.backend.model.Subcategoria;
import br.minhasfinancas.backend.repository.CategoriaRepository;
import br.minhasfinancas.backend.repository.LancamentoRepository;
import br.minhasfinancas.backend.repository.SubcategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CategoriaMapper mapper;

    public CategoriaService(CategoriaRepository categoriaRepository,
            SubcategoriaRepository subcategoriaRepository,
            LancamentoRepository lancamentoRepository,
            CategoriaMapper mapper) {
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.mapper = mapper;
    }

    public List<CategoriaResponseDTO> listarTodos() {
        return categoriaRepository.findAllByOrderByNomeAsc().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        Categoria categoria = mapper.toEntity(dto);
        Categoria categoriaSalva = categoriaRepository.save(categoria);

        Subcategoria subcategoria = new Subcategoria();
        subcategoria.setCategoria(categoriaSalva);

        if (dto.getSubcategoriaNome() != null && !dto.getSubcategoriaNome().trim().isEmpty()) {
            subcategoria.setNome(dto.getSubcategoriaNome().trim());
        } else {
            subcategoria.setNome("Outros");
        }
        subcategoriaRepository.save(subcategoria);
        return mapper.toResponseDTO(categoriaSalva);
    }

    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada");
        }
        if (lancamentoRepository.existsBySubcategoriaCategoriaId(id)) {
            throw new RuntimeException("Não é possível excluir: A categoria possui lançamentos vinculados.");
        }
        List<Subcategoria> subcategorias = subcategoriaRepository.findByCategoriaId(id);
        for (Subcategoria sub : subcategorias) {
            if (lancamentoRepository.existsBySubcategoriaId(sub.getId())) {
                throw new RuntimeException(
                        "Não é possível excluir: Uma subcategoria desta categoria possui lançamentos vinculados.");
            }
        }
        subcategoriaRepository.deleteAll(subcategorias);
        categoriaRepository.deleteById(id);
    }

    @Transactional
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setNome(dto.getNome());
            if (dto.getPermiteDetalhamento() != null) {
                categoria.setPermiteDetalhamento(dto.getPermiteDetalhamento());
            }
            Categoria atualizada = categoriaRepository.save(categoria);
            return mapper.toResponseDTO(atualizada);
        }).orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }
}
