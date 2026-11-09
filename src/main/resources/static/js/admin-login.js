App.onReady(function () {
    var form = document.getElementById("adminLoginForm");
    var alertBox = document.getElementById("formAlert");
    var button = document.getElementById("loginBtn");

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        App.hideAlert(alertBox);

        var email = document.getElementById("email").value.trim();
        var password = document.getElementById("password").value;

        if (!email || !password) {
            App.showAlert(alertBox, "Please enter the admin email and password.", "error");
            return;
        }

        button.disabled = true;
        button.textContent = "Logging in...";

        App.postJson("/api/auth/login", {
            email: email,
            password: password,
            expectedRole: "ADMIN"
        }).then(function () {
            window.location.href = "/admin-dashboard.html";
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
            button.disabled = false;
            button.textContent = "Login as Admin";
        });
    });
});
