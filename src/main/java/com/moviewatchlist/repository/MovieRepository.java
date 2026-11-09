package com.moviewatchlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moviewatchlist.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findAllByOrderByMovieIdDesc();

    List<Movie> findByTitleContainingIgnoreCaseOrderByMovieIdDesc(String title);

    List<Movie> findByGenreIgnoreCaseOrderByMovieIdDesc(String genre);

    List<Movie> findByTitleContainingIgnoreCaseAndGenreIgnoreCaseOrderByMovieIdDesc(
            String title, String genre);

    boolean existsByTitleIgnoreCaseAndReleaseDate(String title, java.time.LocalDate releaseDate);

    @Query("select distinct m.genre from Movie m where m.genre is not null and m.genre <> '' order by m.genre")
    List<String> findDistinctGenres();
}
