package com.moviewatchlist.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.moviewatchlist.entity.Movie;

@Component
public class MovieMapper {

    public Map<String, Object> toMap(Movie movie) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("movieId", movie.getMovieId());
        map.put("title", movie.getTitle());
        map.put("genre", movie.getGenre());
        map.put("description", movie.getDescription());
        map.put("releaseDate", movie.getReleaseDate() == null
                ? null : movie.getReleaseDate().toString());
        map.put("posterPath", movie.getPosterPath());
        return map;
    }

    public List<Map<String, Object>> toList(List<Movie> movies) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Movie movie : movies) {
            result.add(toMap(movie));
        }
        return result;
    }
}
