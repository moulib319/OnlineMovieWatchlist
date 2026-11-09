package com.moviewatchlist.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviewatchlist.dto.WatchStatusRequest;
import com.moviewatchlist.entity.Movie;
import com.moviewatchlist.entity.User;
import com.moviewatchlist.entity.Watchlist;
import com.moviewatchlist.exception.ApiException;
import com.moviewatchlist.repository.MovieRepository;
import com.moviewatchlist.repository.WatchlistRepository;
import com.moviewatchlist.service.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository movieRepository;
    private final CurrentUserService currentUserService;

    public WatchlistController(WatchlistRepository watchlistRepository,
            MovieRepository movieRepository, CurrentUserService currentUserService) {
        this.watchlistRepository = watchlistRepository;
        this.movieRepository = movieRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<Map<String, Object>> myWatchlist() {
        User user = currentUserService.getCurrentUser();
        List<Watchlist> entries = watchlistRepository
                .findByUserUserIdOrderByWatchlistIdDesc(user.getUserId());
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Watchlist entry : entries) {
            result.add(toMap(entry));
        }
        return result;
    }

    @PostMapping("/{movieId}")
    public ResponseEntity<Map<String, Object>> add(@PathVariable Long movieId) {
        User user = currentUserService.getCurrentUser();
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found."));

        if (watchlistRepository.existsByUserUserIdAndMovieMovieId(user.getUserId(), movieId)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This movie is already in your watchlist.");
        }

        Watchlist entry = new Watchlist(user, movie, false);
        watchlistRepository.save(entry);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "\"" + movie.getTitle() + "\" added to your watchlist.");
        body.put("entry", toMap(entry));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PatchMapping("/{movieId}")
    public Map<String, Object> updateStatus(@PathVariable Long movieId,
            @Valid @RequestBody WatchStatusRequest request) {
        User user = currentUserService.getCurrentUser();
        Watchlist entry = watchlistRepository
                .findByUserUserIdAndMovieMovieId(user.getUserId(), movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "This movie is not in your watchlist."));

        entry.setWatchStatus(Boolean.TRUE.equals(request.getWatched()));
        watchlistRepository.save(entry);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "Watch status updated.");
        body.put("entry", toMap(entry));
        return body;
    }

    @DeleteMapping("/{movieId}")
    public Map<String, Object> remove(@PathVariable Long movieId) {
        User user = currentUserService.getCurrentUser();
        Watchlist entry = watchlistRepository
                .findByUserUserIdAndMovieMovieId(user.getUserId(), movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "This movie is not in your watchlist."));

        String title = entry.getMovie().getTitle();
        watchlistRepository.delete(entry);

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("message", "\"" + title + "\" removed from your watchlist.");
        return body;
    }

    private Map<String, Object> toMap(Watchlist entry) {
        Movie movie = entry.getMovie();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("watchlistId", entry.getWatchlistId());
        map.put("movieId", movie.getMovieId());
        map.put("title", movie.getTitle());
        map.put("genre", movie.getGenre());
        map.put("description", movie.getDescription());
        map.put("releaseDate", movie.getReleaseDate() == null
                ? null : movie.getReleaseDate().toString());
        map.put("posterPath", movie.getPosterPath());
        map.put("watched", entry.isWatchStatus());
        return map;
    }
}
