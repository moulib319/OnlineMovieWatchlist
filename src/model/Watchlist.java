package model;

import java.util.ArrayList;
import java.util.TreeSet;

public class Watchlist {
    private ArrayList<Movie> movies;

    public Watchlist() {
        movies = new ArrayList<Movie>();
    }

    public void addMovie(Movie movie) {
        movies.add(movie);
    }

    public boolean removeMovie(Movie movie) {
        return movies.remove(movie);
    }

    public void toggleWatched(Movie movie) {
        movie.setWatched(!movie.isWatched());
    }

    public boolean containsTitle(String title) {
        for (int i = 0; i < movies.size(); i++) {
            if (movies.get(i).getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return movies.size();
    }

    public ArrayList<Movie> getMovies() {
        return new ArrayList<Movie>(movies);
    }

    public ArrayList<String> getGenres() {
        TreeSet<String> genres = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < movies.size(); i++) {
            String genre = movies.get(i).getGenre();
            if (genre != null && genre.trim().length() > 0) {
                genres.add(genre);
            }
        }
        return new ArrayList<String>(genres);
    }

    public ArrayList<Movie> searchByTitle(String keyword) {
        ArrayList<Movie> result = new ArrayList<Movie>();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            if (movie.getTitle().toLowerCase().indexOf(kw) >= 0) {
                result.add(movie);
            }
        }
        return result;
    }

    public ArrayList<Movie> filter(String genre, String status, String keyword) {
        ArrayList<Movie> result = new ArrayList<Movie>();
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);

            if (genre != null && !"All".equals(genre)
                    && !movie.getGenre().equalsIgnoreCase(genre)) {
                continue;
            }
            if ("Watched".equals(status) && !movie.isWatched()) {
                continue;
            }
            if ("Not Watched".equals(status) && movie.isWatched()) {
                continue;
            }
            if (kw.length() > 0 && movie.getTitle().toLowerCase().indexOf(kw) < 0) {
                continue;
            }
            result.add(movie);
        }
        return result;
    }
}
