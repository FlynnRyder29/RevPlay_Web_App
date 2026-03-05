/**
 * playlist-actions.js — Add to Playlist modal (global)
 * Loaded on all pages via layout.html
 *
 * Day 9 fixes:
 *   - Uses PjaxRouter.invalidateCache + PjaxRouter.reload
 *   - String() cast on both playlist IDs
 *   - Document-level click delegation (survives PJAX swaps)
 *   - Re-inits on pjax:complete
 *   - Prevents listener stacking with flags
 */
(function () {
    'use strict';

    function init() {
        var modal = document.getElementById('add-to-playlist-modal');
        if (!modal) return;

        var closeBtn = document.getElementById('btn-close-playlist-modal');
        var songIdInput = document.getElementById('playlist-modal-song-id');
        var container = document.getElementById('playlist-list-container');
        var statusDiv = document.getElementById('playlist-modal-status');
        var newPlaylistInput = document.getElementById('new-playlist-name');
        var createBtn = document.getElementById('btn-create-and-add');
        var csrfToken = (document.querySelector('meta[name="_csrf"]') || {}).content || '';

        if (closeBtn) {
            closeBtn.onclick = closeModal;
        }

        if (createBtn) {
            createBtn.onclick = handleCreate;
        }

        function handleCreate() {
            var name = newPlaylistInput.value.trim();
            var songId = songIdInput.value;

            if (!name) {
                showStatus('Please enter a playlist name', 'error');
                return;
            }

            createBtn.disabled = true;
            createBtn.innerText = 'Creating...';

            fetch('/api/playlists', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken },
                body: JSON.stringify({ name: name, isPublic: true })
            })
                .then(function (res) {
                    if (!res.ok) throw new Error('Failed to create playlist');
                    return res.json();
                })
                .then(function (newPlaylist) {
                    newPlaylistInput.value = '';
                    return addSongToPlaylist(newPlaylist.id, songId);
                })
                .then(function () {
                    fetchPlaylists(songIdInput.value);
                })
                .catch(function (err) {
                    showStatus(err.message, 'error');
                })
                .finally(function () {
                    createBtn.disabled = false;
                    createBtn.innerText = 'Create';
                });
        }

        function openModal(songId) {
            songIdInput.value = songId;
            statusDiv.innerText = '';
            newPlaylistInput.value = '';
            modal.style.display = 'flex';
            fetchPlaylists(songId);
        }

        function closeModal() {
            modal.style.display = 'none';
            songIdInput.value = '';
        }

        function showStatus(msg, type) {
            statusDiv.style.color = type === 'error' ? 'var(--error-color, #f44336)' : '#4CAF50';
            statusDiv.innerText = msg;
        }

        function escapeHtml(str) {
            var div = document.createElement('div');
            div.appendChild(document.createTextNode(str));
            return div.innerHTML;
        }

        function fetchPlaylists(songId) {
            container.innerHTML = '<div style="text-align:center; color:var(--text-muted); padding:20px;">Loading...</div>';

            fetch('/api/playlists/me')
                .then(function (res) {
                    if (!res.ok) throw new Error('Failed');
                    return res.json();
                })
                .then(function (playlists) {
                    if (playlists.length === 0) {
                        container.innerHTML = '<div style="text-align:center; color:var(--text-muted); padding:20px;">No playlists yet. Create one below!</div>';
                        return;
                    }

                    container.innerHTML = '';
                    playlists.forEach(function (pl) {
                        var item = document.createElement('div');
                        item.className = 'pl-modal-item';
                        item.innerHTML =
                            '<span class="pl-modal-item-name">' + escapeHtml(pl.name) + '</span>' +
                            '<span class="pl-modal-item-badge">' + (pl.public ? '🌐' : '🔒') + '</span>';

                        item.addEventListener('click', function () {
                            addSongToPlaylist(pl.id, songId);
                        });
                        container.appendChild(item);
                    });
                })
                .catch(function () {
                    container.innerHTML = '<div style="color:var(--error-color, #f44336); padding:20px; text-align:center;">Error loading playlists.</div>';
                });
        }

        function addSongToPlaylist(playlistId, songId) {
            showStatus('Adding...', 'success');

            return fetch('/api/playlists/' + playlistId + '/songs/' + songId, {
                method: 'POST',
                headers: { 'X-CSRF-TOKEN': csrfToken }
            })
                .then(function (res) {
                    if (!(res.status >= 200 && res.status < 300)) {
                        return res.json().catch(function () { return {}; }).then(function (d) {
                            throw new Error(d.message || 'Song may already be in this playlist.');
                        });
                    }

                    showStatus('✓ Added!', 'success');

                    // Invalidate PJAX cache
                    if (window.PjaxRouter && window.PjaxRouter.invalidateCache) {
                        window.PjaxRouter.invalidateCache('/playlists');
                    }

                    // Check if we're on THIS playlist's detail page
                    var songList = document.getElementById('playlist-song-list');
                    if (songList) {
                        var currentPlaylistId = songList.getAttribute('data-playlist-id');
                        if (String(currentPlaylistId) === String(playlistId)) {
                            setTimeout(function () {
                                if (window.PjaxRouter && window.PjaxRouter.reload) {
                                    window.PjaxRouter.reload();
                                } else {
                                    window.location.reload();
                                }
                            }, 600);
                            return;
                        }
                    }

                    setTimeout(closeModal, 1000);
                })
                .catch(function (err) {
                    showStatus(err.message, 'error');
                });
        }

        // Expose openModal for the document-level click handler
        window._playlistActionsOpenModal = openModal;

        // Close on overlay click
        modal.onclick = function (e) {
            if (e.target === modal) closeModal();
        };
    }

    // ── Document-level click delegation for '+' buttons ──
    // Registered ONCE, survives all PJAX swaps
    if (!window._playlistActionsClickBound) {
        document.addEventListener('click', function (e) {
            var addBtn = e.target.closest('.song-card-add-playlist-btn');
            if (addBtn) {
                e.preventDefault();
                e.stopPropagation();
                var sid = addBtn.getAttribute('data-song-id');
                if (sid && window._playlistActionsOpenModal) {
                    window._playlistActionsOpenModal(sid);
                }
            }
        });
        window._playlistActionsClickBound = true;
    }

    // Run on initial load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Re-run after PJAX content swap
    document.addEventListener('pjax:complete', init);

})();