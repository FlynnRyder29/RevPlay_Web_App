/**
 * favorites.js — RevPlay AJAX Favorite Toggle on Song Cards
 *
 * Day 8: Heart button on song cards — POST/DELETE /api/favorites/{songId}
 *        Pre-marks already-favorited songs on page load.
 */

(function () {
    'use strict';

    // ========================= CSRF TOKEN =========================

    var csrfToken = '';
    var csrfMeta  = document.querySelector('meta[name="_csrf"]');
    if (csrfMeta) csrfToken = csrfMeta.content;

    // ========================= STATE =========================

    var favoritedIds = new Set();   // Set of song IDs that are favorited

    // ========================= INIT =========================

    /**
     * Fetch the user's favorited song IDs and mark buttons accordingly.
     */
    function loadFavorites() {
        fetch('/api/favorites/ids', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
        .then(function (res) {
            if (!res.ok) return [];
            return res.json();
        })
        .then(function (ids) {
            if (!Array.isArray(ids)) return;

            ids.forEach(function (id) {
                favoritedIds.add(id);
            });

            // Mark all existing buttons
            markFavoriteButtons();
        })
        .catch(function () {
            // Silently fail — user might not be authenticated
        });
    }

    /**
     * Mark all .song-card-fav-btn that match favorited IDs.
     */
    function markFavoriteButtons() {
        var btns = document.querySelectorAll('.song-card-fav-btn');
        btns.forEach(function (btn) {
            var songId = parseInt(btn.getAttribute('data-song-id'), 10);
            if (favoritedIds.has(songId)) {
                btn.classList.add('favorited');
                btn.innerHTML = '♥';
                btn.title = 'Remove from Favorites';
            } else {
                btn.classList.remove('favorited');
                btn.innerHTML = '♡';
                btn.title = 'Add to Favorites';
            }
        });
    }

    // ========================= TOGGLE =========================

    /**
     * Toggle favorite for a song via AJAX.
     */
    function toggleFavorite(btn) {
        var songId    = parseInt(btn.getAttribute('data-song-id'), 10);
        var isFav     = favoritedIds.has(songId);
        var method    = isFav ? 'DELETE' : 'POST';
        var url       = '/api/favorites/' + songId;

        // Optimistic UI update
        if (isFav) {
            favoritedIds.delete(songId);
            btn.classList.remove('favorited');
            btn.innerHTML = '♡';
            btn.title = 'Add to Favorites';
        } else {
            favoritedIds.add(songId);
            btn.classList.add('favorited');
            btn.innerHTML = '♥';
            btn.title = 'Remove from Favorites';
        }

        // Add a pop animation
        btn.style.transform = 'scale(1.3)';
        setTimeout(function () {
            btn.style.transform = '';
        }, 200);

        fetch(url, {
            method: method,
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        })
        .then(function (res) {
            if (!res.ok) {
                // Revert on failure
                if (isFav) {
                    favoritedIds.add(songId);
                    btn.classList.add('favorited');
                    btn.innerHTML = '♥';
                } else {
                    favoritedIds.delete(songId);
                    btn.classList.remove('favorited');
                    btn.innerHTML = '♡';
                }
            }
        })
        .catch(function () {
            // Revert on network error
            if (isFav) {
                favoritedIds.add(songId);
                btn.classList.add('favorited');
                btn.innerHTML = '♥';
            } else {
                favoritedIds.delete(songId);
                btn.classList.remove('favorited');
                btn.innerHTML = '♡';
            }
        });
    }

    // ========================= EVENT LISTENERS =========================

    /**
     * Attach click listeners to all favorite buttons (uses event delegation).
     */
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('.song-card-fav-btn');
        if (!btn) return;
        e.preventDefault();
        e.stopPropagation();  // Prevent triggering song card click (play)
        toggleFavorite(btn);
    });

    // ========================= STARTUP =========================

    // Load favorites when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadFavorites);
    } else {
        loadFavorites();
    }

})();
