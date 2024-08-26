package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {
    @Autowired
    private SerieRepository repository;

    public List<SerieDTO> obterTodasSeries() {
        return converteDados(repository.findAll());
    }

    public List<SerieDTO> obterTop5() {
        return converteDados(repository.findTop5ByOrderByAvaliacaoDesc());
    }

    private List<SerieDTO> converteDados(List<Serie> series) {
        return series.stream()
                .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obterLancamentos() {
        return converteDados(repository.lancamentos());
    }

    public SerieDTO obterPorId(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()){
            Serie s = serie.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<EpisodioDTO> obterTodasTemporadas(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()){
            Serie s = serie.get();
            return s.getEpisodios().stream()
                    .map( e -> new EpisodioDTO(e.getId(), e.getTemporada(), e.getTitulo(), e.getNumero()))
                    .toList();
        }
        return null;
    }

    public List<EpisodioDTO> obterTemporadaPornumero(Long id, Integer temporada) {
        return repository.temporadaByNumero(id, temporada)
                .stream()
                .map( e -> new EpisodioDTO(e.getId(), e.getTemporada(), e.getTitulo(), e.getNumero()))
                .toList();
    }

    public List<SerieDTO> obterSeriesporGenero(String genero) {
        Categoria categoria = Categoria.fromPortugues(genero);
        return converteDados(repository.findByGenero(categoria));
    }
}
