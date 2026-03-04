/**
 * theme.js — RevPlay Theme Switcher
 *
 * Two themes:
 *   DARK  → black (#121212) + orange (#d75209)  [default]
 *   LIGHT → cream (#e5e8d1) + teal   (#099c88)
 *
 * Persists in localStorage. Inline script in <head> prevents FOUC.
 */
(function () {
    'use strict';

    var STORAGE_KEY = 'revplay-theme';
    var DARK  = 'dark';
    var LIGHT = 'light';

    // ── Wait for DOM to wire up toggle button ──
    document.addEventListener('DOMContentLoaded', function () {

        var toggle = document.getElementById('theme-toggle');
        if (!toggle) return;

        // Enable smooth CSS transitions AFTER first paint
        // (prevents flash of transition on page load)
        requestAnimationFrame(function () {
            document.body.classList.add('theme-transitions');
        });

        function getCurrentTheme() {
            return document.documentElement.getAttribute('data-theme') || DARK;
        }

        function setTheme(theme) {
            if (theme === LIGHT) {
                document.documentElement.setAttribute('data-theme', LIGHT);
            } else {
                document.documentElement.removeAttribute('data-theme');
            }
            localStorage.setItem(STORAGE_KEY, theme);
            toggle.setAttribute('aria-label',
                theme === DARK ? 'Switch to light mode' : 'Switch to dark mode');
        }

        // Toggle on click
        toggle.addEventListener('click', function () {
            var current = getCurrentTheme();
            setTheme(current === DARK ? LIGHT : DARK);
        });

        // Set initial aria-label
        toggle.setAttribute('aria-label',
            getCurrentTheme() === DARK ? 'Switch to light mode' : 'Switch to dark mode');
    });
})();