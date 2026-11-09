/* Shared helpers for all pages (plain JavaScript, no frameworks). */
var App = {
    request: function (url, options) {
        options = options || {};
        var opts = {
            method: options.method || "GET",
            credentials: "same-origin",
            headers: options.headers || {}
        };
        if (options.body !== undefined && options.body !== null) {
            opts.body = options.body;
        }
        if (opts.body && !(opts.body instanceof FormData) && !opts.headers["Content-Type"]) {
            opts.headers["Content-Type"] = "application/json";
        }
        return fetch(url, opts).then(function (response) {
            var contentType = response.headers.get("content-type") || "";
            var parse = contentType.indexOf("application/json") >= 0
                ? response.json()
                : response.text().then(function (text) {
                    return text ? { message: text } : {};
                });
            return parse.then(function (data) {
                if (!response.ok) {
                    var error = new Error((data && data.message) || "Request failed");
                    error.status = response.status;
                    throw error;
                }
                return data;
            });
        });
    },

    get: function (url) {
        return App.request(url, { method: "GET" });
    },

    postJson: function (url, body) {
        return App.request(url, { method: "POST", body: JSON.stringify(body || {}) });
    },

    patchJson: function (url, body) {
        return App.request(url, { method: "PATCH", body: JSON.stringify(body || {}) });
    },

    postForm: function (url, formData) {
        return App.request(url, { method: "POST", body: formData });
    },

    del: function (url) {
        return App.request(url, { method: "DELETE" });
    },

    logout: function (redirect) {
        return App.postJson("/api/auth/logout", {}).catch(function () {
            /* ignore: we are leaving anyway */
        }).then(function () {
            window.location.href = redirect || "/index.html";
        });
    },

    requireRole: function (role) {
        return App.get("/api/auth/me").then(function (me) {
            if (me.role !== role) {
                window.location.href = role === "ADMIN" ? "/admin-login.html" : "/user-login.html";
                return null;
            }
            var nameNodes = document.querySelectorAll(".js-user-name");
            for (var i = 0; i < nameNodes.length; i++) {
                nameNodes[i].textContent = me.name;
            }
            return me;
        }).catch(function () {
            window.location.href = role === "ADMIN" ? "/admin-login.html" : "/user-login.html";
            return null;
        });
    },

    showAlert: function (element, message, type) {
        if (!element) {
            return;
        }
        element.textContent = message;
        element.className = "alert alert-" + (type || "info") + " show";
    },

    hideAlert: function (element) {
        if (element) {
            element.className = "alert";
            element.textContent = "";
        }
    },

    clearAlerts: function () {
        var alerts = document.querySelectorAll(".alert");
        for (var i = 0; i < alerts.length; i++) {
            alerts[i].className = "alert";
            alerts[i].textContent = "";
        }
    },

    escapeHtml: function (value) {
        if (value === null || value === undefined) {
            return "";
        }
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    },

    poster: function (path) {
        return path ? path : "/images/no-poster.svg";
    },

    formatDate: function (iso) {
        return iso ? iso : "—";
    },

    onReady: function (callback) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", callback);
        } else {
            callback();
        }
    },

    queryParam: function (name) {
        var params = new URLSearchParams(window.location.search);
        return params.get(name);
    }
};

function logout() {
    App.logout();
}
