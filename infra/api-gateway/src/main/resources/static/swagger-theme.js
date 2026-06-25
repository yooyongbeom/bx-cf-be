(function () {
    var STORAGE_KEY = 'bwg-swagger-theme';
    var BTN_ID = 'bwg-theme-btn';

    function isDark() {
        return document.body.classList.contains('bwg-dark');
    }

    function applyTheme(dark) {
        document.body.classList.toggle('bwg-dark', dark);
        var btn = document.getElementById(BTN_ID);
        if (btn) btn.textContent = dark ? '☀ Light' : '☾ Dark';
    }

    function addButton() {
        if (document.getElementById(BTN_ID)) return;

        var btn = document.createElement('button');
        btn.id = BTN_ID;
        btn.textContent = isDark() ? '☀ Light' : '☾ Dark';
        btn.addEventListener('click', function () {
            var next = !isDark();
            localStorage.setItem(STORAGE_KEY, next ? 'dark' : 'light');
            applyTheme(next);
        });
        document.body.appendChild(btn);
    }

    // Swagger UI renders asynchronously — watch DOM until topbar appears
    function watchAndInit() {
        var saved = localStorage.getItem(STORAGE_KEY);
        if (saved === 'dark') applyTheme(true);

        var observer = new MutationObserver(function () {
            addButton();
            // Re-apply theme each time DOM changes (Swagger re-renders on route change)
            if (localStorage.getItem(STORAGE_KEY) === 'dark') applyTheme(true);
        });
        observer.observe(document.body, { childList: true, subtree: true });
        addButton();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', watchAndInit);
    } else {
        watchAndInit();
    }
})();
