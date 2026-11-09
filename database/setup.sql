-- Movie Watchlist Management System - database setup (reference)
-- This script is for a normal MySQL server on port 3306.
-- The project's start-db.bat runs an isolated MySQL instance on port 3307.

CREATE DATABASE IF NOT EXISTS movie_watchlist_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'movieapp'@'localhost' IDENTIFIED BY 'MovieApp@123';
CREATE USER IF NOT EXISTS 'movieapp'@'127.0.0.1' IDENTIFIED BY 'MovieApp@123';
GRANT ALL PRIVILEGES ON movie_watchlist_db.* TO 'movieapp'@'localhost';
GRANT ALL PRIVILEGES ON movie_watchlist_db.* TO 'movieapp'@'127.0.0.1';
FLUSH PRIVILEGES;

USE movie_watchlist_db;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movies (
    movie_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    genre VARCHAR(80) NOT NULL,
    description TEXT,
    release_date DATE,
    poster_path VARCHAR(500),
    PRIMARY KEY (movie_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS watchlist (
    watchlist_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    watch_status TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (watchlist_id),
    UNIQUE KEY uk_watchlist_user_movie (user_id, movie_id),
    KEY fk_watchlist_user (user_id),
    KEY fk_watchlist_movie (movie_id),
    CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_movie FOREIGN KEY (movie_id) REFERENCES movies (movie_id) ON DELETE CASCADE
) ENGINE=InnoDB;
