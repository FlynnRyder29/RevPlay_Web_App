/**
 * favorites.js — RevPlay AJAX Favorite Toggle
 *
 * Day 8: Heart button on song cards — POST/DELETE /api/favorites/{songId}
 *        Pre-marks already-favorited songs on page load.
 * Day 9: PJAX support, player bar heart sync, favorites page card removal.
 * Day 10: Fixed player bar heart — text mode, capture phase delegation,
 *         CSRF refresh, credentials: same-origin on all fetches.
 */
(function () {
    'use strict';

    // ========================= CSRF TOKEN =========================

    var csrfToken = '';

    function refreshCsrfToken() {
        var meta = document.querySelector('meta[name="_csrf"]');
        if (meta) csrfToken = meta.content;
    }

    refreshCsrfToken();

    // ========================= STATE =========================

    var favoritedIds = new Set();
    var loaded = false;
    var pendingSyncSongId = null;

    // ========================= HELPERS =========================

    function getCurrentPlayingSongId() {
        if (window.RevPlay && window.RevPlay.getState) {
            var state = window.RevPlay.getState();
            if (state.currentSong && state.currentSong.id) {
                return Number(state.currentSong.id);
            }
        }
        return null;
    }

    function getSongTitle(sourceBtn, songId) {
        if (sourceBtn) {
            var card = sourceBtn.closest('.song-card');
            if (card) {
                var titleEl = card.querySelector('.song-title');
                if (titleEl) return titleEl.textContent.trim();
            }
        }
        if (window.RevPlay && window.RevPlay.getState) {
            var state = window.RevPlay.getState();
            if (state.currentSong && Number(state.currentSong.id) === Number(songId)) {
                return state.currentSong.title || '';
            }
        }
        return '';
    }

    // ========================= LOAD FAVORITES =========================

    function loadFavorites() {
        refreshCsrfToken();

        fetch('/api/favorites/ids', {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        })
            .then(function (res) {
                if (!res.ok) return [];
                return res.json();
            })
            .then(function (ids) {
                if (!Array.isArray(ids)) return;

                favoritedIds.clear();
                ids.forEach(function (id) {
                    favoritedIds.add(Number(id));
                });

                loaded = true;
                markFavoriteButtons();
                forcePlayerHeartSync();

                if (pendingSyncSongId !== null) {
                    forcePlayerHeartSync();
                    pendingSyncSongId = null;
                }
            })
            .catch(function () {
                // Silently fail
            });
    }

    function markFavoriteButtons() {
        var btns = document.querySelectorAll('.song-card-fav-btn');
        btns.forEach(function (btn) {
            var songId = Number(btn.getAttribute('data-song-id'));
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

    // ========================= PLAYER BAR HEART =========================

    function forcePlayerHeartSync() {
        var btn = document.getElementById('player-favorite');
        if (!btn) return;

        var currentSongId = getCurrentPlayingSongId();
        if (!currentSongId) return;

        if (!loaded) {
            pendingSyncSongId = currentSongId;
            return;
        }

        btn.style.display = 'inline-block';

        var isFav = favoritedIds.has(Number(currentSongId));
        applyHeartState(btn, isFav);
    }

    function applyHeartState(btn, isFav) {
        if (!btn) btn = document.getElementById('player-favorite');
        if (!btn) return;

        var img = btn.querySelector('.player-icon-img');
        var fallback = btn.querySelector('.icon-fallback');

        // Always force text mode
        if (img) img.style.display = 'none';
        if (fallback) fallback.style.display = 'inline';

        if (isFav) {
            btn.classList.add('favorited');
            btn.title = 'Remove from Favorites';
            if (fallback) {
                fallback.textContent = '♥';
                fallback.style.color = '#ff4757';
                fallback.style.fontSize = '18px';
            }
        } else {
            btn.classList.remove('favorited');
            btn.title = 'Add to Favorites';
            if (fallback) {
                fallback.textContent = '♡';
                fallback.style.color = '';
                fallback.style.fontSize = '';
            }
        }
    }

    // ========================= TOGGLE =========================

    function toggleFavorite(songId, sourceBtn) {
        songId = Number(songId);
        if (!songId || isNaN(songId)) return;

        refreshCsrfToken();

        var wasFav = favoritedIds.has(songId);
        var method = wasFav ? 'DELETE' : 'POST';
        var url = '/api/favorites/' + songId;

        // Optimistic UI
        if (wasFav) {
            favoritedIds.delete(songId);
        } else {
            favoritedIds.add(songId);
        }

        updateAllCardButtons(songId);

        // Sync player bar heart
        var currentSongId = getCurrentPlayingSongId();
        if (currentSongId && Number(currentSongId) === songId) {
            applyHeartState(null, favoritedIds.has(songId));
        }

        // Pop animation
        if (sourceBtn) {
            sourceBtn.style.transform = 'scale(1.3)';
            setTimeout(function () { sourceBtn.style.transform = ''; }, 200);
        }

        var onFavoritesPage = window.location.pathname === '/favorites'
            || window.location.pathname.indexOf('/favorites') === 0;

        var songTitle = getSongTitle(sourceBtn, songId);

        fetch(url, {
            method: method,
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        })
            .then(function (res) {
                if (!(res.status >= 200 && res.status < 300)) {
                    revertToggle(songId, wasFav);
                    if (window.RevPlay && window.RevPlay.toast) {
                        RevPlay.toast({ type: 'error', message: 'Failed to update favorites' });
                    }
                    return;
                }

                if (window.RevPlay && window.RevPlay.toast) {
                    if (wasFav) {
                        RevPlay.toast({ type: 'info', message: songTitle ? '"' + songTitle + '" removed from favorites' : 'Removed from favorites', duration: 2500 });
                    } else {
                        RevPlay.toast({ type: 'success', message: songTitle ? '"' + songTitle + '" added to favorites' : 'Added to favorites', duration: 2500 });
                    }
                }

                if (onFavoritesPage && wasFav) {
                    removeFavoriteCard(songId);
                }

                if (window.PjaxRouter && window.PjaxRouter.invalidateCache) {
                    window.PjaxRouter.invalidateCache('/favorites');
                }
            })
            .catch(function () {
                revertToggle(songId, wasFav);
                if (window.RevPlay && window.RevPlay.toast) {
                    RevPlay.toast({ type: 'error', message: 'Network error — please try again' });
                }
            });
    }

    function revertToggle(songId, wasFav) {
        if (wasFav) {
            favoritedIds.add(songId);
        } else {
            favoritedIds.delete(songId);
        }
        updateAllCardButtons(songId);

        var currentSongId = getCurrentPlayingSongId();
        if (currentSongId && Number(currentSongId) === songId) {
            applyHeartState(null, favoritedIds.has(songId));
        }
    }

    function updateAllCardButtons(songId) {
        songId = Number(songId);
        var isFav = favoritedIds.has(songId);
        var btns = document.querySelectorAll('.song-card-fav-btn[data-song-id="' + songId + '"]');
        btns.forEach(function (btn) {
            if (isFav) {
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

    function removeFavoriteCard(songId) {
        var card = document.querySelector('.song-card[data-id="' + songId + '"]')
            || document.querySelector('.song-card-fav-btn[data-song-id="' + songId + '"]');

        if (card) {
            var songCard = card.closest('.song-card') || card;

            songCard.style.transition = 'opacity 0.3s, transform 0.3s';
            songCard.style.opacity = '0';
            songCard.style.transform = 'scale(0.9)';

            setTimeout(function () {
                songCard.remove();

                var remaining = document.querySelectorAll('.song-card').length;
                var subtitle = document.querySelector('.hero-subtitle');
                if (subtitle) subtitle.textContent = remaining + ' saved songs';

                if (remaining === 0) {
                    var grid = document.querySelector('.song-grid');
                    if (grid) grid.remove();
                    var actionBtns = document.querySelector('#btn-play-all-favs');
                    if (actionBtns && actionBtns.parentElement) actionBtns.parentElement.remove();

                    var contentArea = document.getElementById('favorites-content');
                    if (contentArea) {
                        var emptyDiv = document.createElement('div');
                        emptyDiv.className = 'empty-state';
                        emptyDiv.style.textAlign = 'center';
                        emptyDiv.style.marginTop = '60px';
                        emptyDiv.innerHTML =
                            '<div style="font-size: 48px; margin-bottom: 20px;">🎬</div>' +
                            '<h2>No favorites yet</h2>' +
                            '<p style="color: var(--text-muted);">Find some music you love and click the heart icon.</p>' +
                            '<a href="/search" class="btn-primary" style="margin-top: 20px; display: inline-block;">Discover Music</a>';
                        contentArea.appendChild(emptyDiv);
                    }
                }

                forcePlayerHeartSync();
            }, 300);
        }
    }

    // ========================= SINGLE DELEGATION LISTENER =========================

    if (!window._favClickBound) {
        document.addEventListener('click', function (e) {

            // Check 1: Song card heart
            var cardBtn = e.target.closest('.song-card-fav-btn');
            if (cardBtn) {
                e.preventDefault();
                e.stopPropagation();
                var songId = cardBtn.getAttribute('data-song-id');
                if (songId) toggleFavorite(songId, cardBtn);
                return;
            }

            // Check 2: Player bar heart — walk up from click target
            var el = e.target;
            while (el && el !== document.body) {
                if (el.id === 'player-favorite') {
                    e.preventDefault();
                    e.stopPropagation();

                    var currentSongId = getCurrentPlayingSongId();
                    if (currentSongId) {
                        toggleFavorite(currentSongId, el);
                    }
                    return;
                }
                el = el.parentElement;
            }

        }, true); // CAPTURE PHASE

        window._favClickBound = true;
    }

    // ========================= EXPOSE FOR PJAX =========================

    window.initFavorites = function () {
        refreshCsrfToken();
        if (loaded) markFavoriteButtons();
        forcePlayerHeartSync();
        loadFavorites();
    };

    window.syncFavoritesForSong = function (songId) {
        if (!songId) return;
        if (!loaded) {
            pendingSyncSongId = Number(songId);
            return;
        }
        forcePlayerHeartSync();
    };

    window.isSongFavorited = function (songId) {
        return favoritedIds.has(Number(songId));
    };

    // ========================= STARTUP =========================

    function init() {
        refreshCsrfToken();
        forcePlayerHeartSync();
        loadFavorites();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    document.addEventListener('pjax:complete', function () {
        refreshCsrfToken();
        markFavoriteButtons();
        forcePlayerHeartSync();
    });

})();