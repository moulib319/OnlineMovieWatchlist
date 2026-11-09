App.onReady(function () {
    var form = document.getElementById("registerForm");
    var alertBox = document.getElementById("formAlert");
    var button = document.getElementById("registerBtn");

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        App.hideAlert(alertBox);

        var name = document.getElementById("name").value.trim();
        var email = document.getElementById("email").value.trim();
        var password = document.getElementById("password").value;
        var confirmPassword = document.getElementById("confirmPassword").value;

        if (!name) {
            App.showAlert(alertBox, "Please enter your full name.", "error");
            return;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            App.showAlert(alertBox, "Please enter a valid email address.", "error");
            return;
        }
        if (password.length < 6) {
            App.showAlert(alertBox, "Password must be at least 6 characters.", "error");
            return;
        }
        if (password !== confirmPassword) {
            App.showAlert(alertBox, "Passwords do not match.", "error");
            return;
        }

        button.disabled = true;
        button.textContent = "Registering...";

        App.postJson("/api/auth/register", {
            name: name,
            email: email,
            password: password
        }).then(function (response) {
            App.showAlert(alertBox, response.message || "Registration successful.", "success");
            form.reset();
            button.textContent = "Registered";
            window.setTimeout(function () {
                window.location.href = "/user-login.html";
            }, 1200);
        }, function (error) {
            App.showAlert(alertBox, error.message, "error");
            button.disabled = false;
            button.textContent = "Register";
        });
    });
});
