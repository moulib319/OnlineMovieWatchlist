package com.moviewatchlist.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moviewatchlist.entity.Movie;
import com.moviewatchlist.entity.User;
import com.moviewatchlist.exception.ApiException;
import com.moviewatchlist.repository.MovieRepository;
import com.moviewatchlist.repository.UserRepository;
import com.moviewatchlist.repository.WatchlistRepository;
import com.moviewatchlist.service.MovieMapper;
import com.moviewatchlist.service.PosterStorage;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;
    private final PosterStorage posterStorage;
    private final MovieMapper movieMapper;

    public AdminController(MovieRepository movieRepository, UserRepository userRepository,
            WatchlistRepository watchlistRepository, PosterStorage posterStorage,
            MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.watchlistRepository = watchlistRepository;
        this.posterStorage = posterStorage;
        this.movieMapper = movieMapper;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("movieCount", movieRepository.count());
        body.put("userCount", userRepository.count());
        return body;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (User user : users) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("userId", user.getUserId());
            map.put("name", user.getName());
            map.put("email", user.getEmail());
            map.put("role", user.getRole());
            result.add(map);
        }
        return result;
    }

    @GetMapping("/movies")
    public List<Map<String, Object>> movies(@RequestParam(required = false) String search) {
        String keyword = search == null ? "" : search.trim();
        List<Movie> movies;
        if (keyword.length() > 0) {
            movies = movieRepository.findByTitleContainingIgnoreCaseOrderByMovieIdDesc(keyword);
        } else {
            movies = movieRepository.findAllByOrderByMovieIdDesc();
        }
        return movieMapper.toList(movies);
    }

    @PostMapping(value = "/movies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> addMovie(
            @RequestParam String title,
            @RequestParam String genre,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
            @RequestParam(required = false) MultipartFile poster) {

        String cleanTitle = requireText(title, "Title");
        String cleanGenre = requireText(genre, "Genre");
        String cleanDescription = requireText(description, "Description");
        if (releaseDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Release date is required.");
        }

        String posterPath = posterStorage.save(poster);
        Movie movie = new Movie(cleanTitle, cleanGenre, cleanDescription, releaseDate, posterPath);
        movieRepository.save(movie);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Movie added successfully.");
        body.put("movie", movieMapper.toMap(movie));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping(value = "/movies/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateMovie(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String genre,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
            @RequestParam(required = false) MultipartFile poster) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found."));

        String cleanTitle = requireText(title, "Title");
        String cleanGenre = requireText(genre, "Genre");
        String cleanDescription = requireText(description, "Description");
        if (releaseDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Release date is required.");
        }

        String oldPoster = movie.getPosterPath();
        if (poster != null && !poster.isEmpty()) {
            String newPoster = posterStorage.save(poster);
            movie.setPosterPath(newPoster);
            posterStorage.delete(oldPoster);
        }

        movie.setTitle(cleanTitle);
        movie.setGenre(cleanGenre);
        movie.setDescription(cleanDescription);
        movie.setReleaseDate(releaseDate);
        movieRepository.save(movie);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Movie updated successfully.");
        body.put("movie", movieMapper.toMap(movie));
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/movies/{id}")
    public Map<String, Object> deleteMovie(@PathVariable Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found."));

        watchlistRepository.deleteByMovieMovieId(id);
        posterStorage.delete(movie.getPosterPath());
        movieRepository.delete(movie);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Movie deleted successfully.");
        return body;
    }

    private String requireText(String value, String field) {
        if (value == null || value.trim().length() == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, field + " is required.");
        }
        return value.trim();
    }
}
