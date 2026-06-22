package br.minhasfinancas.backend.service;

import br.minhasfinancas.backend.dto.SubcategoriaRequestDTO;
import br.minhasfinancas.backend.dto.SubcategoriaResponseDTO;
import br.minhasfinancas.backend.mapper.SubcategoriaMapper;
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
public class SubcategoriaService {

    private final SubcategoriaRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final SubcategoriaMapper mapper;

    public SubcategoriaService(SubcategoriaRepository repository, 
                               CategoriaRepository categoriaRepository, 
                               LancamentoRepository lancamentoRepository,
                               SubcategoriaMapper mapper) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.mapper = mapper;
    }

    public List<SubcategoriaResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SubcategoriaResponseDTO> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId).stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubcategoriaResponseDTO criar(SubcategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Subcategoria subcategoria = mapper.toEntity(dto);
        subcategoria.setCategoria(categoria);
        return mapper.toResponseDTO(repository.save(subcategoria));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Subcategoria não encontrada");
        }
        if (lancamentoRepository.existsBySubcategoriaId(id)) {
            throw new RuntimeException("Não é possível excluir: A subcategoria possui lançamentos vinculados.");
        }
        repository.deleteById(id);
    }
}
