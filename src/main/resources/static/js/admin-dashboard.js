App.onReady(function () {
    var alertBox = document.getElementById("pageAlert");

    App.requireRole("ADMIN").then(function (me) {
        if (!me) {
            return;
        }
        loadStats();
        loadUsers();
    });

    function loadStats() {
        App.get("/api/admin/stats").then(function (data) {
            document.getElementById("movieCount").textContent = data.movieCount;
            document.getElementById("userCount").textContent = data.userCount;
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
        });
    }

    function loadUsers() {
        var tbody = document.getElementById("usersTableBody");
        App.get("/api/admin/users").then(function (users) {
            if (!users.length) {
                tbody.innerHTML = '<tr><td colspan="4" class="empty">No registered users yet.</td></tr>';
                return;
            }
            var html = "";
            for (var i = 0; i < users.length; i++) {
                var user = users[i];
                var roleClass = user.role === "ADMIN" ? "badge-role-admin" : "badge-role-user";
                html += "<tr>"
                    + "<td>" + (i + 1) + "</td>"
                    + "<td>" + App.escapeHtml(user.name) + "</td>"
                    + "<td>" + App.escapeHtml(user.email) + "</td>"
                    + '<td><span class="badge ' + roleClass + '">'
                    + App.escapeHtml(user.role) + "</span></td>"
                    + "</tr>";
            }
            tbody.innerHTML = html;
        }, function (error) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty">Could not load users.</td></tr>';
            App.showAlert(alertBox, error.message, "error");
        });
    }
});
