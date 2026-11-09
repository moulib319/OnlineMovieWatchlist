App.onReady(function () {
    var alertBox = document.getElementById("pageAlert");
    var grid = document.getElementById("watchlistGrid");
    var statusFilter = document.getElementById("statusFilter");
    var searchInput = document.getElementById("searchInput");

    var entries = [];

    App.requireRole("USER").then(function (me) {
        if (me) {
            loadWatchlist();
        }
    });

    function loadWatchlist() {
        App.get("/api/watchlist").then(function (list) {
            entries = list;
            applyFilter();
        }, function (error) {
            grid.innerHTML = '<p class="empty">Could not load your watchlist.</p>';
            App.showAlert(alertBox, error.message, "error");
        });
    }

    function applyFilter() {
        var status = statusFilter.value;
        var keyword = searchInput.value.trim().toLowerCase();
        var filtered = [];
        for (var i = 0; i < entries.length; i++) {
            var entry = entries[i];
            if (status === "Watched" && !entry.watched) {
                continue;
            }
            if (status === "Not Watched" && entry.watched) {
                continue;
            }
            if (keyword && entry.title.toLowerCase().indexOf(keyword) < 0) {
                continue;
            }
            filtered.push(entry);
        }
        render(filtered);
    }

    function render(list) {
        if (!entries.length) {
            grid.innerHTML = '<p class="empty">Your watchlist is empty. '
                + '<a href="/user-dashboard.html">Browse movies</a> to add some.</p>';
            return;
        }
        if (!list.length) {
            grid.innerHTML = '<p class="empty">No movies match the current filter.</p>';
            return;
        }
        var html = "";
        for (var i = 0; i < list.length; i++) {
            var entry = list[i];
            var badgeClass = entry.watched ? "badge-watched" : "badge-notwatched";
            var badgeText = entry.watched ? "Watched" : "Not Watched";
            var toggleText = entry.watched ? "Mark as Not Watched" : "Mark as Watched";
            html += '<article class="movie-card">'
                + '<img class="poster" src="' + App.escapeHtml(App.poster(entry.posterPath))
                + '" alt="Poster of ' + App.escapeHtml(entry.title) + '">'
                + '<div class="movie-info">'
                + '<h3 class="movie-title">' + App.escapeHtml(entry.title) + "</h3>"
                + '<span class="movie-genre">' + App.escapeHtml(entry.genre) + "</span>"
                + '<span class="badge ' + badgeClass + '">' + badgeText + "</span>"
                + '<div class="card-actions">'
                + '<button type="button" class="btn btn-small toggle-btn" data-id="' + entry.movieId + '">'
                + toggleText + "</button>"
                + '<button type="button" class="btn btn-small btn-danger remove-btn" data-id="' + entry.movieId + '">Remove</button>'
                + "</div></div></article>";
        }
        grid.innerHTML = html;
    }

    function findEntry(id) {
        for (var i = 0; i < entries.length; i++) {
            if (String(entries[i].movieId) === String(id)) {
                return entries[i];
            }
        }
        return null;
    }

    function toggleStatus(id) {
        var entry = findEntry(id);
        if (!entry) {
            return;
        }
        App.patchJson("/api/watchlist/" + id, { watched: !entry.watched }).then(function (response) {
            App.showAlert(alertBox, response.message || "Watch status updated.", "success");
            loadWatchlist();
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
        });
    }

    function removeEntry(id) {
        var entry = findEntry(id);
        if (!entry) {
            return;
        }
        if (!window.confirm('Remove "' + entry.title + '" from your watchlist?')) {
            return;
        }
        App.del("/api/watchlist/" + id).then(function (response) {
            App.showAlert(alertBox, response.message || "Removed from watchlist.", "success");
            loadWatchlist();
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
        });
    }

    grid.addEventListener("click", function (event) {
        var button = event.target.closest("button");
        if (!button) {
            return;
        }
        var id = button.getAttribute("data-id");
        if (button.classList.contains("toggle-btn")) {
            toggleStatus(id);
        } else if (button.classList.contains("remove-btn")) {
            removeEntry(id);
        }
    });

    statusFilter.addEventListener("change", applyFilter);
    searchInput.addEventListener("input", applyFilter);
});
