package br.com.alura.screenmatch.dto;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public record SerieDTO(long id,
                       String titulo,
                       Integer temporadas,
                       Double avaliacao,
                       Categoria genero,
                       String atores,
                       String poster,
                       String sinopse) {
}
