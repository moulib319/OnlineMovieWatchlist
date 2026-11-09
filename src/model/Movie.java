package model;

public class Movie {
    private String title;
    private String genre;
    private String description;
    private boolean watched;

    public Movie(String title, String genre, String description, boolean watched) {
        this.title = title;
        this.genre = genre;
        this.description = description;
        this.watched = watched;
    }

    public Movie(String title, String genre, String description) {
        this(title, genre, description, false);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    @Override
    public String toString() {
        return title;
    }
}
