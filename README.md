# Movie Watchlist Management System

A B.Tech CSE academic project built with **Java + Spring Boot**, **HTML/CSS/vanilla JavaScript**
and **MySQL**. Users can browse movies and maintain a personal watchlist. An administrator can
add, edit and delete movies, upload posters, and view registered users.

## Technologies

- Backend: Java 17+ (built with JDK 27), Spring Boot 4.1.2
- Security: Spring Security with BCrypt password hashing
- Database: MySQL 8 with Spring Data JPA
- Frontend: HTML, CSS and basic JavaScript (no React/Angular)
- Build tool: Maven Wrapper (`mvnw.cmd`)

## Login credentials

| Role | Email | Password |
| --- | --- | --- |
| Administrator | `admin@9398moviewatchlist.com` | `Dhana@8186` |

Users create their own accounts from the Register page.

## How to run in VS Code (Windows)

1. **Start MySQL.** Double-click `start-db.bat` in this folder.
   It starts an isolated MySQL 8 instance on `127.0.0.1:3307` using the MySQL already installed
   at `C:\Program Files\MySQL\MySQL Server 8.0`. You can leave the window running (or close it;
   the database keeps running in the background).
   > The first run creates the data folder under `%LOCALAPPDATA%\MovieWatchlist\data`.
   > Your existing MySQL80 service on port 3306 is not touched.

2. **Open the project in VS Code** (`File > Open Folder`).

3. **Run the application.** Open a terminal in VS Code and run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   The first run downloads Maven and the dependencies, so it may take a few minutes.

4. **Open Google Chrome** and go to:

   ```
   http://localhost:8080/
   ```

5. **Admin login:** click *Admin Login* and sign in with the admin credentials above.
   **User:** click *Register*, create an account, then log in.

6. When finished, you can stop the application with `Ctrl+C` in the VS Code terminal and stop
   the database by double-clicking `stop-db.bat`.

## Main features

### Administrator

- Separate admin login and dashboard
- View all movies and registered users
- Add movies with title, genre, description, release date and poster image
- Edit movie details or replace the poster
- Delete movies (also removes them from every watchlist)
- Search movies by title
- Logout

### User

- Register and log in
- Browse available movies
- Search movies by title and filter by genre
- View movie details (poster, genre, release date, description)
- Add movies to a personal watchlist (duplicates are blocked)
- Remove movies from the watchlist
- Mark movies as Watched / Not Watched
- View the personal watchlist with coloured status badges
- Logout

## Pages

| Admin pages | User pages |
| --- | --- |
| `admin-login.html` | `register.html` |
| `admin-dashboard.html` | `user-login.html` |
| `add-movie.html` | `user-dashboard.html` |
| `manage-movies.html` | `my-watchlist.html` |

There is also a landing page at `index.html`.

## Database

- Database name: `movie_watchlist_db`
- Tables: `users`, `movies`, `watchlist`
- The tables are created/updated automatically by JPA on startup (`ddl-auto=update`).
- A reference SQL script is available at `database/setup.sql`.
- Each user has a separate watchlist. A unique constraint on `(user_id, movie_id)` prevents
  duplicate movies in the same user's watchlist.

### Connection settings

Defined in `src/main/resources/application.properties`:

```
spring.datasource.url=jdbc:mysql://127.0.0.1:3307/movie_watchlist_db...
spring.datasource.username=movieapp
spring.datasource.password=MovieApp@123
```

The isolated local MySQL root password is `Root@1234`.

If you prefer to use your own MySQL server on port 3306, run `database/setup.sql`, change the
URL port to `3306`, and update the username/password in `application.properties`.

## Project structure

```
src/main/java/com/moviewatchlist/
  MovieWatchlistApplication.java
  entity/       User, Movie, Watchlist
  repository/   UserRepository, MovieRepository, WatchlistRepository
  controller/   AuthController, AdminController, MovieController, WatchlistController
  dto/          RegisterRequest, LoginRequest, WatchStatusRequest
  config/       SecurityConfig, CustomUserDetailsService, DataSeeder, WebConfig
  service/      CurrentUserService, PosterStorage, MovieMapper
  exception/    ApiException, GlobalExceptionHandler
src/main/resources/
  application.properties
  static/       HTML pages, css/style.css, js/*.js, images
uploads/        Uploaded movie posters (created automatically)
database/setup.sql
start-db.bat / stop-db.bat
```

## Notes

- Passwords are stored as BCrypt hashes, never in plain text.
- Only the administrator can add, edit or delete movies. Users can only manage their own watchlist.
- Poster images are stored in the `uploads/` folder and served from `/uploads/...`.
- The original desktop Swing version of the project is kept in `src/Main.java`, `src/model`,
  `src/storage`, `src/ui` and `run.bat`. It is independent of the Spring Boot application.
- CSRF protection is disabled to keep the plain-JavaScript `fetch` calls simple; this is a
  deliberate choice for a student project.

## Troubleshooting

- **"Communications link failure" / cannot connect**: make sure `start-db.bat` is running and
  port 3307 is listening. Check `%LOCALAPPDATA%\MovieWatchlist\data\server.log`.
- **Port 3307 already in use**: another process is using it. Stop that process or change
  `--port=3307` in `start-db.bat`, `stop-db.bat` and the datasource URL.
- **MySQL not found**: update the `BASE` path in `start-db.bat` and `stop-db.bat`.
- **Clean build**: `.\mvnw.cmd clean package -DskipTests`
