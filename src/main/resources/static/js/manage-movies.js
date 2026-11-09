App.onReady(function () {
    var alertBox = document.getElementById("pageAlert");
    var tbody = document.getElementById("moviesTableBody");
    var searchInput = document.getElementById("searchInput");
    var searchBtn = document.getElementById("searchBtn");
    var clearBtn = document.getElementById("clearBtn");

    var editModal = document.getElementById("editModal");
    var modalAlert = document.getElementById("modalAlert");
    var editForm = document.getElementById("editMovieForm");
    var updateBtn = document.getElementById("updateBtn");

    var moviesList = [];
    var currentSearch = "";

    App.requireRole("ADMIN").then(function (me) {
        if (me) {
            loadMovies("");
        }
    });

    function loadMovies(keyword) {
        currentSearch = keyword || "";
        var url = "/api/admin/movies";
        if (currentSearch) {
            url += "?search=" + encodeURIComponent(currentSearch);
        }
        App.get(url).then(function (movies) {
            moviesList = movies;
            renderTable(movies);
        }, function (error) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty">Could not load movies.</td></tr>';
            App.showAlert(alertBox, error.message, "error");
        });
    }

    function renderTable(movies) {
        if (!movies.length) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty">No movies found. Add a movie to get started.</td></tr>';
            return;
        }
        var html = "";
        for (var i = 0; i < movies.length; i++) {
            var movie = movies[i];
            html += "<tr>"
                + '<td><img class="poster-thumb" src="' + App.escapeHtml(App.poster(movie.posterPath))
                + '" alt="Poster"></td>'
                + "<td>" + App.escapeHtml(movie.title) + "</td>"
                + "<td>" + App.escapeHtml(movie.genre) + "</td>"
                + "<td>" + App.escapeHtml(App.formatDate(movie.releaseDate)) + "</td>"
                + '<td><div class="table-actions">'
                + '<button type="button" class="btn btn-small edit-btn" data-id="' + movie.movieId + '">Edit</button>'
                + '<button type="button" class="btn btn-small btn-danger delete-btn" data-id="' + movie.movieId + '">Delete</button>'
                + "</div></td>"
                + "</tr>";
        }
        tbody.innerHTML = html;
    }

    function findMovie(id) {
        for (var i = 0; i < moviesList.length; i++) {
            if (String(moviesList[i].movieId) === String(id)) {
                return moviesList[i];
            }
        }
        return null;
    }

    function openEdit(id) {
        var movie = findMovie(id);
        if (!movie) {
            return;
        }
        App.hideAlert(modalAlert);
        document.getElementById("editId").value = movie.movieId;
        document.getElementById("editTitle").value = movie.title || "";
        document.getElementById("editGenre").value = movie.genre || "";
        document.getElementById("editReleaseDate").value = movie.releaseDate || "";
        document.getElementById("editDescription").value = movie.description || "";
        document.getElementById("editPoster").value = "";
        editModal.classList.add("open");
    }

    function closeEdit() {
        editModal.classList.remove("open");
    }

    function deleteMovie(id) {
        var movie = findMovie(id);
        if (!movie) {
            return;
        }
        if (!window.confirm('Delete "' + movie.title + '" from the catalogue?\nIt will also be removed from all watchlists.')) {
            return;
        }
        App.del("/api/admin/movies/" + id).then(function (response) {
            App.showAlert(alertBox, response.message || "Movie deleted.", "success");
            loadMovies(currentSearch);
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
        });
    }

    tbody.addEventListener("click", function (event) {
        var button = event.target.closest("button");
        if (!button) {
            return;
        }
        var id = button.getAttribute("data-id");
        if (button.classList.contains("edit-btn")) {
            openEdit(id);
        } else if (button.classList.contains("delete-btn")) {
            deleteMovie(id);
        }
    });

    searchBtn.addEventListener("click", function () {
        loadMovies(searchInput.value.trim());
    });

    searchInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            loadMovies(searchInput.value.trim());
        }
    });

    clearBtn.addEventListener("click", function () {
        searchInput.value = "";
        loadMovies("");
    });

    document.getElementById("modalClose").addEventListener("click", closeEdit);
    document.getElementById("cancelEdit").addEventListener("click", closeEdit);

    editModal.addEventListener("click", function (event) {
        if (event.target === editModal) {
            closeEdit();
        }
    });

    editForm.addEventListener("submit", function (event) {
        event.preventDefault();
        App.hideAlert(modalAlert);

        var id = document.getElementById("editId").value;
        var title = document.getElementById("editTitle").value.trim();
        var genre = document.getElementById("editGenre").value.trim();
        var releaseDate = document.getElementById("editReleaseDate").value;
        var description = document.getElementById("editDescription").value.trim();
        var posterInput = document.getElementById("editPoster");

        if (!title || !genre || !description || !releaseDate) {
            App.showAlert(modalAlert, "Please fill in all fields.", "error");
            return;
        }

        var data = new FormData();
        data.append("title", title);
        data.append("genre", genre);
        data.append("releaseDate", releaseDate);
        data.append("description", description);
        if (posterInput.files.length > 0) {
            var file = posterInput.files[0];
            if (file.type.indexOf("image/") !== 0) {
                App.showAlert(modalAlert, "The poster must be an image file.", "error");
                return;
            }
            if (file.size > 5 * 1024 * 1024) {
                App.showAlert(modalAlert, "The poster must be smaller than 5 MB.", "error");
                return;
            }
            data.append("poster", file);
        }

        updateBtn.disabled = true;
        updateBtn.textContent = "Updating...";

        App.postForm("/api/admin/movies/" + id, data).then(function (response) {
            updateBtn.disabled = false;
            updateBtn.textContent = "Update Movie";
            closeEdit();
            App.showAlert(alertBox, response.message || "Movie updated.", "success");
            loadMovies(currentSearch);
        }, function (error) {
            updateBtn.disabled = false;
            updateBtn.textContent = "Update Movie";
            App.showAlert(modalAlert, error.message, "error");
        });
    });
});
