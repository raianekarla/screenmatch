package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;


public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=60922ffe";
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar Series buscada
                    4 - Buscar Serie por titulo
                    5 - Buscar Series por ator
                    6 - Top 5 series
                    7 - Buscar por categotia
                    8 - Filtrar séries por temporadas e avaliação
                    9 - Buscar epsodio por trecho
                    10- Top 5 episodios por serie
                    11- Buscar buscar episodios a partir de uma data
                                    
                    0 - Sair                                 
                    """;
            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerie();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscada();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    buscarTop5series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorTemporada();
                    break;
                case 9:
                    buscarEpisodioProTrecho();
                    break;
                case 10:
                    buscarTop5EpisodioPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção invalida");
            }
        }
    }

    private void buscarSerie() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dadosSerie = conversor.obterDados(json, DadosSerie.class);
        return dadosSerie;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscada();
        System.out.println("Escolhar uma serie:");
        var nomeSerie = leitura.nextLine();

        var serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo()
                        .replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);

        } else {
            System.out.println("Serie não encontrada");
        }
    }

    private void listarSeriesBuscada() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolhar uma serie:");
        var nomeSerie = leitura.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da serie: " + serieBusca.get());
        } else {
            System.out.println("Serie não encontrada");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o nome do ator:");
        var nomeAtor = leitura.nextLine();
        System.out.println("Digite avaliacao minima que deve pesquisar:");
        var avaliacao = leitura.nextDouble();
        List<Serie> serieList = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println("Séries que o ator " + nomeAtor + " participou: ");
        serieList.forEach(s -> System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5series() {
        List<Serie> seriesTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        seriesTop.forEach(s -> System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Digite o genero para busca:");
        var genero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series da categoria " + genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSeriesPorTemporada() {
        System.out.println("Digite a quantidade máxima de temporadas:");
        var numTemporadas = leitura.nextInt();
        System.out.println("Digite a avaliação minima:");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesPorTemporadas = repositorio
                .seriesPorTemporadaEAvaliacao(numTemporadas, avaliacao);
        System.out.println("Series com o numero maximo de "
                + numTemporadas + " temporadas e com avaliação igual ou maoir que "
                + avaliacao + ":");
        seriesPorTemporadas.forEach(System.out::println);
    }

    private void buscarEpisodioProTrecho() {
        System.out.println("Digite o nome do episódio para busca:");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrado = repositorio.episodioPorTrecho(trechoEpisodio);
        System.out.println("Episodio(s) encontrado(s):");
        episodiosEncontrado.forEach(System.out::println);
    }


    private void buscarTop5EpisodioPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> episodioList = repositorio.episodioTop5PorSeries(serie);
            episodioList.forEach(System.out::println);
        }
    }

    private void buscarEpisodiosDepoisData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite para pesquisa:");
            var anoLancamento = leitura.nextLine();
            List<Episodio> episodioListAno = repositorio.episodioAnoLimite(serie, anoLancamento);
            episodioListAno.forEach(System.out::println);
        }
    }

}
