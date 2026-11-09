package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import model.Movie;
import model.Watchlist;

public class FileStorage {

    public static Watchlist load(File file) {
        Watchlist watchlist = new Watchlist();
        if (file == null || !file.exists()) {
            return watchlist;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] parts = splitLine(line);
                if (parts.length != 4) {
                    continue;
                }
                String title = parts[0];
                String genre = parts[1];
                String description = parts[2];
                boolean watched = "true".equalsIgnoreCase(parts[3]);
                if (title.trim().length() == 0) {
                    continue;
                }
                watchlist.addMovie(new Movie(title, genre, description, watched));
            }
        } catch (IOException e) {
            System.err.println("Could not read watchlist: " + e.getMessage());
        } finally {
            closeQuietly(reader);
        }
        return watchlist;
    }

    public static void save(Watchlist watchlist, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            ArrayList<Movie> movies = watchlist.getMovies();
            for (int i = 0; i < movies.size(); i++) {
                Movie movie = movies.get(i);
                writer.write(escape(movie.getTitle()));
                writer.write("|");
                writer.write(escape(movie.getGenre()));
                writer.write("|");
                writer.write(escape(movie.getDescription()));
                writer.write("|");
                writer.write(movie.isWatched() ? "true" : "false");
                writer.newLine();
            }
        } finally {
            closeQuietly(writer);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\n' || c == '\r') {
                sb.append(' ');
            } else {
                if (c == '\\' || c == '|') {
                    sb.append('\\');
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String[] splitLine(String line) {
        ArrayList<String> fields = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[fields.size()]);
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                System.err.println("Could not close file: " + e.getMessage());
            }
        }
    }
}
