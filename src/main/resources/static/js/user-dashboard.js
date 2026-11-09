App.onReady(function () {
    var alertBox = document.getElementById("pageAlert");
    var grid = document.getElementById("movieGrid");
    var searchInput = document.getElementById("searchInput");
    var genreFilter = document.getElementById("genreFilter");
    var searchBtn = document.getElementById("searchBtn");
    var clearBtn = document.getElementById("clearBtn");

    var detailsModal = document.getElementById("detailsModal");
    var detailsAlert = document.getElementById("detailsAlert");
    var detailAddBtn = document.getElementById("detailAddBtn");

    var moviesList = [];
    var selectedMovieId = null;

    App.requireRole("USER").then(function (me) {
        if (me) {
            loadGenres();
            loadMovies();
        }
    });

    function loadGenres() {
        App.get("/api/movies/genres").then(function (genres) {
            var current = genreFilter.value;
            genreFilter.innerHTML = '<option value="All">All Genres</option>';
            for (var i = 0; i < genres.length; i++) {
                var option = document.createElement("option");
                option.value = genres[i];
                option.textContent = genres[i];
                genreFilter.appendChild(option);
            }
            genreFilter.value = current || "All";
            if (genreFilter.value !== (current || "All")) {
                genreFilter.value = "All";
            }
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
        });
    }

    function loadMovies() {
        var keyword = searchInput.value.trim();
        var genre = genreFilter.value;
        var url = "/api/movies?search=" + encodeURIComponent(keyword)
            + "&genre=" + encodeURIComponent(genre);

        App.get(url).then(function (movies) {
            moviesList = movies;
            renderMovies(movies);
        }, function (error) {
            grid.innerHTML = '<p class="empty">Could not load movies.</p>';
            App.showAlert(alertBox, error.message, "error");
        });
    }

    function renderMovies(movies) {
        if (!movies.length) {
            grid.innerHTML = '<p class="empty">No movies found. Try a different search or genre.</p>';
            return;
        }
        var html = "";
        for (var i = 0; i < movies.length; i++) {
            var movie = movies[i];
            html += '<article class="movie-card">'
                + '<img class="poster" src="' + App.escapeHtml(App.poster(movie.posterPath))
                + '" alt="Poster of ' + App.escapeHtml(movie.title) + '">'
                + '<div class="movie-info">'
                + '<h3 class="movie-title">' + App.escapeHtml(movie.title) + "</h3>"
                + '<span class="movie-genre">' + App.escapeHtml(movie.genre) + "</span>"
                + '<div class="card-actions">'
                + '<button type="button" class="btn btn-small detail-btn" data-id="' + movie.movieId + '">View Details</button>'
                + '<button type="button" class="btn btn-small btn-success add-btn" data-id="' + movie.movieId + '">Add to Watchlist</button>'
                + "</div></div></article>";
        }
        grid.innerHTML = html;
    }

    function findMovie(id) {
        for (var i = 0; i < moviesList.length; i++) {
            if (String(moviesList[i].movieId) === String(id)) {
                return moviesList[i];
            }
        }
        return null;
    }

    function openDetails(id) {
        var movie = findMovie(id);
        if (!movie) {
            return;
        }
        selectedMovieId = movie.movieId;
        App.hideAlert(detailsAlert);
        document.getElementById("detailPoster").src = App.poster(movie.posterPath);
        document.getElementById("detailPoster").alt = "Poster of " + movie.title;
        document.getElementById("detailTitle").textContent = movie.title;
        document.getElementById("detailGenre").textContent = movie.genre;
        document.getElementById("detailDate").textContent = App.formatDate(movie.releaseDate);
        document.getElementById("detailDescription").textContent = movie.description || "No description available.";
        detailAddBtn.disabled = false;
        detailAddBtn.textContent = "Add to Watchlist";
        detailsModal.classList.add("open");
    }

    function closeDetails() {
        detailsModal.classList.remove("open");
    }

    function addToWatchlist(movieId, targetAlert, button) {
        if (button) {
            button.disabled = true;
            button.textContent = "Adding...";
        }
        App.postJson("/api/watchlist/" + movieId, {}).then(function (response) {
            App.showAlert(targetAlert, response.message || "Added to watchlist.", "success");
            if (button) {
                button.textContent = "Added";
            }
        }, function (error) {
            App.showAlert(targetAlert, error.message, "error");
            if (button) {
                button.disabled = false;
                button.textContent = "Add to Watchlist";
            }
        });
    }

    grid.addEventListener("click", function (event) {
        var button = event.target.closest("button");
        if (!button) {
            return;
        }
        var id = button.getAttribute("data-id");
        if (button.classList.contains("detail-btn")) {
            openDetails(id);
        } else if (button.classList.contains("add-btn")) {
            addToWatchlist(id, alertBox, button);
        }
    });

    detailAddBtn.addEventListener("click", function () {
        if (selectedMovieId !== null) {
            addToWatchlist(selectedMovieId, detailsAlert, detailAddBtn);
        }
    });

    document.getElementById("detailsClose").addEventListener("click", closeDetails);
    detailsModal.addEventListener("click", function (event) {
        if (event.target === detailsModal) {
            closeDetails();
        }
    });

    searchBtn.addEventListener("click", loadMovies);
    searchInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            loadMovies();
        }
    });
    genreFilter.addEventListener("change", loadMovies);
    clearBtn.addEventListener("click", function () {
        searchInput.value = "";
        genreFilter.value = "All";
        loadMovies();
    });
});
