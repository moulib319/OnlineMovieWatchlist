package com.moviewatchlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.moviewatchlist.entity.Watchlist;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findByUserUserIdOrderByWatchlistIdDesc(Long userId);

    Optional<Watchlist> findByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    boolean existsByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    long countByUserUserId(Long userId);

    long countByUserUserIdAndWatchStatus(Long userId, boolean watchStatus);

    @Transactional
    void deleteByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    @Transactional
    void deleteByMovieMovieId(Long movieId);
}
