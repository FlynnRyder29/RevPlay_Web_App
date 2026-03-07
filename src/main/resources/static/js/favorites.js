/**
 * favorites.js — RevPlay AJAX Favorite Toggle
 *
 * Day 8: Heart button on song cards — POST/DELETE /api/favorites/{songId}
 *        Pre-marks already-favorited songs on page load.
 * Day 9: PJAX support, player bar heart sync, favorites page card removal,
 *        exposed initFavorites globally for navigation.js reinit.
 * Day 10: Fixed player bar heart inconsistency — unified img+fallback update,
 *         added pending queue for sync calls before favorites load.
 */
(function () {
    'use strict';

    // ========================= CSRF TOKEN =========================

    var csrfToken = '';
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    if (csrfMeta) csrfToken = csrfMeta.content;

    // ========================= STATE =========================

    var favoritedIds = new Set();
    var loaded = false;
    var pendingSyncSongId = null; // Queue sync calls that arrive before load completes

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

                // If a sync was requested before load finished, honor it now
                if (pendingSyncSongId !== null) {
                    syncPlayerBarHeart();
                    pendingSyncSongId = null;
                }
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
        if (!playerFav) return;

        // Get current song ID from RevPlay
        var currentSongId = getCurrentPlayingSongId();

        if (!currentSongId) return;

        // If favorites haven't loaded yet, store the request for later
        if (!loaded) {
            pendingSyncSongId = currentSongId;
            return;
        }

        var isFav = favoritedIds.has(Number(currentSongId));
        setPlayerHeartUI(playerFav, isFav);
    }

    /**
     * Get the currently playing song ID from RevPlay state.
     */
    function getCurrentPlayingSongId() {
        if (window.RevPlay && window.RevPlay.getState) {
            var state = window.RevPlay.getState();
            if (state.currentSong && state.currentSong.id) {
                return Number(state.currentSong.id);
            }
        }
        return null;
    }

    /**
     * Update the player bar heart button visual.
     * Handles BOTH the <img> element AND the <span> fallback,
     * regardless of which one is currently visible.
     */
    function setPlayerHeartUI(btn, isFav) {
        if (!btn) return;

        var img = btn.querySelector('.player-icon-img');
        var fallback = btn.querySelector('.icon-fallback');

        if (isFav) {
            btn.classList.add('favorited');
            btn.title = 'Remove from Favorites';

            // Update fallback text
            if (fallback) fallback.textContent = '♥';

            // Update/hide img and show fallback with filled heart
            // Since there's no "filled heart" icon image, we switch to text-based display
            if (img) {
                img.style.display = 'none';
            }
            if (fallback) {
                fallback.style.display = 'inline';
                fallback.style.color = '#ff4757';
                fallback.style.fontSize = '18px';
            }
        } else {
            btn.classList.remove('favorited');
            btn.title = 'Add to Favorites';

            // Update fallback text
            if (fallback) {
                fallback.textContent = '♡';
                fallback.style.color = '';
                fallback.style.fontSize = '';
            }

            // Restore original display: try to show img, hide fallback
            if (img) {
                // Test if the image had previously errored
                if (img.naturalWidth === 0 && img.complete) {
                    // Image failed to load — keep fallback visible
                    img.style.display = 'none';
                    if (fallback) fallback.style.display = 'inline';
                } else {
                    // Image is fine — show it, hide fallback
                    img.style.display = '';
                    if (fallback) fallback.style.display = 'none';
                }
            } else {
                // No img element — show fallback
                if (fallback) fallback.style.display = 'inline';
            }
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
        var currentSongId = getCurrentPlayingSongId();
        if (currentSongId && Number(currentSongId) === songId) {
            var playerFav = document.getElementById('player-favorite');
            setPlayerHeartUI(playerFav, favoritedIds.has(songId));
        }

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

        // Get song title for toast (from nearest song card or player)
        var songTitle = getSongTitle(sourceBtn, songId);

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
                    revertToggle(songId, isFav);

                    if (window.RevPlay && window.RevPlay.toast) {
                        RevPlay.toast({ type: 'error', message: 'Failed to update favorites' });
                    }
                    return;
                }

                // Success toast
                if (window.RevPlay && window.RevPlay.toast) {
                    if (isFav) {
                        RevPlay.toast({
                            type: 'info',
                            message: songTitle ? '"' + songTitle + '" removed from favorites' : 'Removed from favorites',
                            duration: 2500
                        });
                    } else {
                        RevPlay.toast({
                            type: 'success',
                            message: songTitle ? '"' + songTitle + '" added to favorites' : 'Added to favorites',
                            duration: 2500
                        });
                    }
                }

                // If on favorites page and we just unfavorited, remove the card
                if (onFavoritesPage && isFav) {
                    removeFavoriteCard(songId);
                }

                // Invalidate PJAX cache for favorites page
                if (window.PjaxRouter && window.PjaxRouter.invalidateCache) {
                    window.PjaxRouter.invalidateCache('/favorites');
                }
            })
            .catch(function () {
                revertToggle(songId, isFav);

                if (window.RevPlay && window.RevPlay.toast) {
                    RevPlay.toast({ type: 'error', message: 'Network error — please try again' });
                }
            });
    }

    /**
     * Revert an optimistic toggle on failure.
     */
    function revertToggle(songId, wasFav) {
        if (wasFav) {
            favoritedIds.add(songId);
        } else {
            favoritedIds.delete(songId);
        }
        updateAllCardButtons(songId);

        // Revert player bar heart too
        var currentSongId = getCurrentPlayingSongId();
        if (currentSongId && Number(currentSongId) === songId) {
            var playerFav = document.getElementById('player-favorite');
            setPlayerHeartUI(playerFav, favoritedIds.has(songId));
        }
    }

    /**
     * Get song title from the source button's card, or from the player state.
     */
    function getSongTitle(sourceBtn, songId) {
        // Try from card
        if (sourceBtn) {
            var card = sourceBtn.closest('.song-card');
            if (card) {
                var titleEl = card.querySelector('.song-title');
                if (titleEl) return titleEl.textContent.trim();
            }
        }

        // Try from player state (for player bar heart clicks)
        if (window.RevPlay && window.RevPlay.getState) {
            var state = window.RevPlay.getState();
            if (state.currentSong && Number(state.currentSong.id) === songId) {
                return state.currentSong.title || '';
            }
        }

        return '';
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

                    // Also remove action buttons
                    var actionBtns = document.querySelector('#btn-play-all-favs');
                    if (actionBtns) {
                        var actionBar = actionBtns.parentElement;
                        if (actionBar) actionBar.remove();
                    }

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
            var playerFavBtn = e.target.closest('#player-favorite');
            if (!playerFavBtn) return;
            e.preventDefault();
            e.stopPropagation();

            // Get current song ID from RevPlay
            var currentSongId = getCurrentPlayingSongId();

            if (currentSongId) {
                toggleFavorite(currentSongId, playerFavBtn);
            }
        });
        window._favPlayerClickBound = true;
    }

    // ========================= EXPOSE FOR PJAX =========================

    /**
     * Called by navigation.js reinitComponents() after every PJAX swap.
     */
    window.initFavorites = function () {
        if (loaded) {
            markFavoriteButtons();
            syncPlayerBarHeart();
        }
        loadFavorites();
    };

    /**
     * Called by player.js when a new song starts playing.
     * Syncs the player bar heart with the favorites state.
     */
    window.syncFavoritesForSong = function (songId) {
        if (!songId) return;

        if (!loaded) {
            // Favorites haven't loaded yet — queue the sync
            pendingSyncSongId = Number(songId);
            return;
        }

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