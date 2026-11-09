App.onReady(function () {
    var form = document.getElementById("addMovieForm");
    var alertBox = document.getElementById("formAlert");
    var button = document.getElementById("saveBtn");

    App.requireRole("ADMIN");

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        App.hideAlert(alertBox);

        var title = document.getElementById("title").value.trim();
        var genre = document.getElementById("genre").value.trim();
        var releaseDate = document.getElementById("releaseDate").value;
        var description = document.getElementById("description").value.trim();
        var posterInput = document.getElementById("poster");

        if (!title || !genre || !description || !releaseDate) {
            App.showAlert(alertBox, "Please fill in title, genre, release date and description.", "error");
            return;
        }
        if (posterInput.files.length > 0) {
            var file = posterInput.files[0];
            if (file.type.indexOf("image/") !== 0) {
                App.showAlert(alertBox, "The poster must be an image file.", "error");
                return;
            }
            if (file.size > 5 * 1024 * 1024) {
                App.showAlert(alertBox, "The poster must be smaller than 5 MB.", "error");
                return;
            }
        }

        var data = new FormData();
        data.append("title", title);
        data.append("genre", genre);
        data.append("releaseDate", releaseDate);
        data.append("description", description);
        if (posterInput.files.length > 0) {
            data.append("poster", posterInput.files[0]);
        }

        button.disabled = true;
        button.textContent = "Saving...";

        App.postForm("/api/admin/movies", data).then(function (response) {
            App.showAlert(alertBox, response.message || "Movie added successfully.", "success");
            form.reset();
            button.textContent = "Saved";
            window.setTimeout(function () {
                window.location.href = "/manage-movies.html";
            }, 1200);
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
            button.disabled = false;
            button.textContent = "Save Movie";
        });
    });
});
