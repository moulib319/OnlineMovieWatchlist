package com.moviewatchlist.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moviewatchlist.entity.Movie;
import com.moviewatchlist.exception.ApiException;
import com.moviewatchlist.repository.MovieRepository;
import com.moviewatchlist.service.MovieMapper;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieController(MovieRepository movieRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    @GetMapping
    public List<Map<String, Object>> list(@RequestParam(required = false) String search,
            @RequestParam(required = false) String genre) {
        String keyword = search == null ? "" : search.trim();
        boolean hasGenre = genre != null && genre.trim().length() > 0
                && !"All".equalsIgnoreCase(genre.trim());

        List<Movie> movies;
        if (keyword.length() > 0 && hasGenre) {
            movies = movieRepository
                    .findByTitleContainingIgnoreCaseAndGenreIgnoreCaseOrderByMovieIdDesc(
                            keyword, genre.trim());
        } else if (keyword.length() > 0) {
            movies = movieRepository.findByTitleContainingIgnoreCaseOrderByMovieIdDesc(keyword);
        } else if (hasGenre) {
            movies = movieRepository.findByGenreIgnoreCaseOrderByMovieIdDesc(genre.trim());
        } else {
            movies = movieRepository.findAllByOrderByMovieIdDesc();
        }
        return movieMapper.toList(movies);
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return movieRepository.findDistinctGenres();
    }

    @GetMapping("/{id}")
    public Map<String, Object> details(@PathVariable Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found."));
        return movieMapper.toMap(movie);
    }
}
