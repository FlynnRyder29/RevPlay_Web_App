/**
 * favorites.js — RevPlay AJAX Favorite Toggle
 *
 * Day 8: Heart button on song cards — POST/DELETE /api/favorites/{songId}
 *        Pre-marks already-favorited songs on page load.
 * Day 9: PJAX support, player bar heart sync, favorites page card removal,
 *        exposed initFavorites globally for navigation.js reinit.
 */
(function () {
    'use strict';

    // ========================= CSRF TOKEN =========================

    var csrfToken = '';
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    if (csrfMeta) csrfToken = csrfMeta.content;

    // ========================= STATE =========================

    var favoritedIds = new Set();
    var loaded = false; // Track if we've fetched IDs at least once

    // ========================= INIT =========================

    /**
     * Fetch the user's favorited song IDs and mark buttons accordingly.
     * Safe to call multiple times (after PJAX navigations).
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

                // Rebuild the set from server truth
                favoritedIds.clear();
                ids.forEach(function (id) {
                    favoritedIds.add(Number(id));
                });

                loaded = true;
                markFavoriteButtons();
                syncPlayerBarHeart();
            })
            .catch(function () {
                // Silently fail — user might not be authenticated
            });
    }

    /**
     * Mark all .song-card-fav-btn that match favorited IDs.
     * Also works after PJAX content swap (new cards in DOM).
     */
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

    /**
     * Sync the player bar heart with the currently playing song.
     */
    function syncPlayerBarHeart() {
        var playerFav = document.getElementById('player-favorite');
        if (!playerFav || playerFav.style.display === 'none') return;

        // Get current song ID from RevPlay
        var currentSongId = null;
        if (window.RevPlay && window.RevPlay.getState) {
            var state = window.RevPlay.getState();
            if (state.currentSong && state.currentSong.id) {
                currentSongId = Number(state.currentSong.id);
            }
        }

        if (currentSongId && favoritedIds.has(currentSongId)) {
            setPlayerHeartUI(playerFav, true);
        } else if (currentSongId) {
            setPlayerHeartUI(playerFav, false);
        }
    }

    /**
     * Update the player bar heart button visual.
     */
    function setPlayerHeartUI(btn, isFav) {
        if (!btn) return;
        var fallback = btn.querySelector('.icon-fallback');
        var img = btn.querySelector('.player-icon-img');

        if (isFav) {
            btn.classList.add('favorited');
            btn.title = 'Remove from Favorites';
            if (fallback) fallback.textContent = '♥';
            // If using image, could swap src — but fallback text is simpler
        } else {
            btn.classList.remove('favorited');
            btn.title = 'Add to Favorites';
            if (fallback) fallback.textContent = '♡';
        }
    }

    // ========================= TOGGLE =========================

    /**
     * Toggle favorite for a song via AJAX.
     * Works for both song card hearts AND the player bar heart.
     */
    function toggleFavorite(songId, sourceBtn) {
        songId = Number(songId);
        if (!songId || isNaN(songId)) return;

        var isFav = favoritedIds.has(songId);
        var method = isFav ? 'DELETE' : 'POST';
        var url = '/api/favorites/' + songId;

        // Optimistic UI update
        if (isFav) {
            favoritedIds.delete(songId);
        } else {
            favoritedIds.add(songId);
        }

        // Update ALL matching song card buttons (same song may appear in multiple places)
        updateAllCardButtons(songId);

        // Update player bar heart if this song is currently playing
        syncPlayerBarHeart();

        // Pop animation on source button
        if (sourceBtn) {
            sourceBtn.style.transform = 'scale(1.3)';
            setTimeout(function () {
                sourceBtn.style.transform = '';
            }, 200);
        }

        // Check if we're on the favorites page — remove card after unfavorite
        var onFavoritesPage = window.location.pathname === '/favorites'
            || window.location.pathname.indexOf('/favorites') === 0;

        // Get song title for toast (from nearest song card)
        var songTitle = '';
        if (sourceBtn) {
            var card = sourceBtn.closest('.song-card');
            if (card) {
                var titleEl = card.querySelector('.song-title');
                if (titleEl) songTitle = titleEl.textContent.trim();
            }
        }

        fetch(url, {
            method: method,
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            }
        })
            .then(function (res) {
                if (!(res.status >= 200 && res.status < 300)) {
                    // Revert on failure
                    if (isFav) {
                        favoritedIds.add(songId);
                    } else {
                        favoritedIds.delete(songId);
                    }
                    updateAllCardButtons(songId);
                    syncPlayerBarHeart();

                    if (window.RevPlay && window.RevPlay.toast) {
                        RevPlay.toast({ type: 'error', message: 'Failed to update favorites' });
                    }
                    return;
                }

                // Success toast
                if (window.RevPlay && window.RevPlay.toast) {
                    if (isFav) {
                        // Was favorited, now removed
                        RevPlay.toast({
                            type: 'info',
                            message: songTitle ? '"' + songTitle + '" removed from favorites' : 'Removed from favorites',
                            duration: 2500
                        });
                    } else {
                        // Was not favorited, now added
                        RevPlay.toast({
                            type: 'success',
                            message: songTitle ? '"' + songTitle + '" added to favorites' : 'Added to favorites',
                            duration: 2500
                        });
                    }
                }

                // Success — if on favorites page and we just unfavorited, remove the card
                if (onFavoritesPage && isFav) {
                    removeFavoriteCard(songId);
                }

                // Invalidate PJAX cache for favorites page
                if (window.PjaxRouter && window.PjaxRouter.invalidateCache) {
                    window.PjaxRouter.invalidateCache('/favorites');
                }
            })
            .catch(function () {
                // Revert on network error
                if (isFav) {
                    favoritedIds.add(songId);
                } else {
                    favoritedIds.delete(songId);
                }
                updateAllCardButtons(songId);
                syncPlayerBarHeart();

                if (window.RevPlay && window.RevPlay.toast) {
                    RevPlay.toast({ type: 'error', message: 'Network error — please try again' });
                }
            });
    }

    /**
     * Update all song card favorite buttons for a given song ID.
     */
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

    /**
     * Animate and remove a song card from the favorites page after unfavoriting.
     */
    function removeFavoriteCard(songId) {
        var card = document.querySelector('.song-card[data-id="' + songId + '"]')
            || document.querySelector('.song-card-fav-btn[data-song-id="' + songId + '"]');

        if (card) {
            // Find the actual .song-card wrapper
            var songCard = card.closest('.song-card') || card;

            songCard.style.transition = 'opacity 0.3s, transform 0.3s';
            songCard.style.opacity = '0';
            songCard.style.transform = 'scale(0.9)';

            setTimeout(function () {
                songCard.remove();

                // Update count in hero subtitle
                var remaining = document.querySelectorAll('.song-card').length;
                var subtitle = document.querySelector('.hero-subtitle');
                if (subtitle) {
                    subtitle.textContent = remaining + ' saved songs';
                }

                // Show empty state if no more songs
                if (remaining === 0) {
                    var grid = document.querySelector('.song-grid');
                    if (grid) grid.remove();

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
            }, 300);
        }
    }

    // ========================= EVENT LISTENERS =========================

    // Song card heart buttons — document-level delegation (survives PJAX)
    if (!window._favCardClickBound) {
        document.addEventListener('click', function (e) {
            var btn = e.target.closest('.song-card-fav-btn');
            if (!btn) return;
            e.preventDefault();
            e.stopPropagation();
            var songId = btn.getAttribute('data-song-id');
            if (songId) toggleFavorite(songId, btn);
        });
        window._favCardClickBound = true;
    }

    // Player bar heart button — document-level delegation (survives PJAX)
    if (!window._favPlayerClickBound) {
        document.addEventListener('click', function (e) {
            var playerFav = e.target.closest('#player-favorite');
            if (!playerFav) return;
            e.preventDefault();
            e.stopPropagation();

            // Get current song ID from RevPlay
            var currentSongId = null;
            if (window.RevPlay && window.RevPlay.getState) {
                var state = window.RevPlay.getState();
                if (state.currentSong && state.currentSong.id) {
                    currentSongId = state.currentSong.id;
                }
            }

            if (currentSongId) {
                toggleFavorite(currentSongId, playerFav);
            }
        });
        window._favPlayerClickBound = true;
    }

    // ========================= EXPOSE FOR PJAX =========================

    /**
     * Called by navigation.js reinitComponents() after every PJAX swap.
     * Re-marks favorite buttons on new DOM elements using cached favoritedIds.
     * Also re-fetches from server to catch changes made on other pages.
     */
    window.initFavorites = function () {
        if (loaded) {
            // We already have IDs cached — immediately mark buttons
            markFavoriteButtons();
            syncPlayerBarHeart();
        }
        // Also refresh from server (catches changes from other tabs/pages)
        loadFavorites();
    };

    /**
     * Called by player.js when a new song starts playing.
     * Syncs the player bar heart with the favorites state.
     */
    window.syncFavoritesForSong = function (songId) {
        if (!songId) return;
        syncPlayerBarHeart();
    };

    /**
     * Check if a song is favorited (used by player.js).
     */
    window.isSongFavorited = function (songId) {
        return favoritedIds.has(Number(songId));
    };

    // ========================= STARTUP =========================

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadFavorites);
    } else {
        loadFavorites();
    }

    // Re-init after PJAX content swap
    document.addEventListener('pjax:complete', function () {
        markFavoriteButtons();
        syncPlayerBarHeart();
    });

})();