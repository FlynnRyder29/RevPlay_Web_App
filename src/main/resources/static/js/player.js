/**
 * player.js — RevPlay HTML5 Audio Player
 *
 * Day 5: play/pause, seek, volume, prev/next, song-card click, now-playing.
 * Day 7: playlist row click, history recording, favorites toggle,
 *         keyboard shortcuts, queue display, drag-seek progress bar.
 */
(function () {
    'use strict';

    // ========================= DOM REFERENCES =========================

    var audio        = document.getElementById('audio-element');
    var btnPlay      = document.getElementById('btn-play');
    var btnPrev      = document.getElementById('btn-prev');
    var btnNext      = document.getElementById('btn-next');
    var btnShuffle   = document.getElementById('btn-shuffle');
    var btnRepeat    = document.getElementById('btn-repeat');
    var progressContainer = document.getElementById('progress-container');
    var progressFill = document.getElementById('progress-fill');
    var timeCurrent  = document.getElementById('time-current');
    var timeTotal    = document.getElementById('time-total');
    var playerTitle  = document.getElementById('player-title');
    var playerArtist = document.getElementById('player-artist');
    var playerArt    = document.getElementById('player-art');
    var playerFav    = document.getElementById('player-favorite');

    // If the player-bar elements don't exist on this page, bail out
    if (!audio || !btnPlay) return;

    // ========================= PLAYER STATE =========================

    var queue         = [];    // Array of { id, url, title, artist, cover }
    var currentIndex  = -1;
    var isPlaying     = false;
    var isShuffle     = false;
    var repeatMode    = 0;     // 0 = off, 1 = repeat all, 2 = repeat one
    var isFavorited   = false; // current song favorite state

    // CSRF token for AJAX calls (read from <meta> tag)
    var csrfToken = '';
    var csrfMeta  = document.querySelector('meta[name="_csrf"]');
    if (csrfMeta) csrfToken = csrfMeta.content;

    // ========================= UTILITY FUNCTIONS =========================

    function formatTime(seconds) {
        if (isNaN(seconds) || seconds < 0) return '0:00';
        var mins = Math.floor(seconds / 60);
        var secs = Math.floor(seconds % 60);
        return mins + ':' + (secs < 10 ? '0' : '') + secs;
    }

    /**
     * Build the queue from all .song-card elements currently in the DOM.
     */
    function buildQueueFromCards() {
        var cards = document.querySelectorAll('.song-card');
        queue = [];
        cards.forEach(function (card) {
            queue.push({
                id:     card.dataset.id     || '',
                url:    card.dataset.url     || '',
                title:  card.dataset.title   || 'Unknown Title',
                artist: card.dataset.artist  || 'Unknown Artist',
                cover:  card.querySelector('img') ? card.querySelector('img').src : ''
            });
        });
    }

    /**
     * Build the queue from .playlist-song-row elements (playlist detail page).
     */
    function buildQueueFromPlaylistRows() {
        var rows = document.querySelectorAll('.playlist-song-row');
        queue = [];
        rows.forEach(function (row) {
            var coverImg = row.querySelector('.song-row-cover');
            queue.push({
                id:     row.dataset.id     || '',
                url:    row.dataset.url     || '',
                title:  row.dataset.title   || 'Unknown Title',
                artist: row.dataset.artist  || 'Unknown Artist',
                cover:  coverImg ? coverImg.src : ''
            });
        });
    }

    // ========================= ICON HELPERS =========================

    function setPlayIcon(button, playing) {
        if (!button) return;
        var img = button.querySelector('.player-play-img')
            || button.querySelector('.page-play-icon');
        var fallback = button.querySelector('.play-fallback');

        if (img && img.style.display !== 'none') {
            img.src = playing
                ? (img.dataset.pauseSrc || '/images/icons/pause.png')
                : (img.dataset.playSrc  || '/images/icons/play.png');
            img.alt = playing ? '⏸' : '▶';
        } else if (fallback) {
            fallback.textContent = playing ? '⏸' : '▶';
        }
        button.title = playing ? 'Pause' : 'Play';
    }

    function setRepeatIcon(button, mode) {
        if (!button) return;
        if (mode === 0) {
            button.classList.remove('active');
        } else {
            button.classList.add('active');
        }
        var img = button.querySelector('.player-repeat-img');
        var fallback = button.querySelector('.repeat-fallback');
        if (img && img.style.display !== 'none') {
            img.src = mode === 2
                ? (img.dataset.repeatOneSrc || '/images/icons/repeat-one.png')
                : (img.dataset.repeatSrc    || '/images/icons/repeat.png');
            img.alt = mode === 2 ? '🔂' : '🔁';
        } else if (fallback) {
            fallback.textContent = mode === 2 ? '🔂' : '🔁';
        }
    }

    function setShuffleIcon(button, isActive) {
        if (!button) return;
        button.classList.toggle('active', isActive);
    }

    // ========================= API HELPERS =========================

    /**
     * Record listening history via POST /api/history
     */
    function recordHistory(songId) {
        if (!songId) return;
        fetch('/api/history', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ songId: parseInt(songId) })
        }).catch(function (err) {
            console.warn('Failed to record history:', err.message);
        });
    }

    /**
     * Toggle favorite for current song
     */
    function toggleFavorite() {
        if (currentIndex < 0 || currentIndex >= queue.length) return;
        var songId = queue[currentIndex].id;
        if (!songId) return;

        var method = isFavorited ? 'DELETE' : 'POST';
        fetch('/api/favorites/' + songId, {
            method: method,
            headers: { 'X-CSRF-TOKEN': csrfToken }
        })
            .then(function (res) {
                if (res.ok) {
                    isFavorited = !isFavorited;
                    updateFavoriteUI();
                }
            })
            .catch(function (err) {
                console.warn('Favorite toggle failed:', err.message);
            });
    }

    function updateFavoriteUI() {
        if (!playerFav) return;
        if (isFavorited) {
            playerFav.classList.add('favorited');
            playerFav.title = 'Remove from Favorites';
        } else {
            playerFav.classList.remove('favorited');
            playerFav.title = 'Add to Favorites';
        }
    }

    // ========================= CORE PLAYBACK =========================

    function playSongAtIndex(index) {
        if (index < 0 || index >= queue.length) return;

        currentIndex = index;
        var song = queue[currentIndex];

        if (song.url) {
            audio.src = song.url;
            audio.load();
            audio.play()
                .then(function () {
                    isPlaying = true;
                    setPlayIcon(btnPlay, true);
                    var pageBtnPlay = document.getElementById('page-btn-play');
                    setPlayIcon(pageBtnPlay, true);
                })
                .catch(function (err) {
                    console.warn('Playback failed:', err.message);
                });
        }

        // Update now-playing info in the global player bar
        if (playerTitle)  playerTitle.textContent  = song.title;
        if (playerArtist) playerArtist.textContent = song.artist;
        if (playerArt) {
            playerArt.src = song.cover || 'https://www.shutterstock.com/shutterstock/photos/2626947211/display_1500/stock-vector-jazz-music-poster-design-template-2626947211.jpg';
            playerArt.style.display = 'block';
        }
        if (playerFav) playerFav.style.display = 'inline-block';

        // Reset favorite state for new song
        isFavorited = false;
        updateFavoriteUI();

        // Update dedicated player page
        updatePlayerPage(song);

        // Highlight active card/row
        highlightActiveCard(song.id);
        highlightActiveRow(song.id);

        // Record to listening history
        recordHistory(song.id);

        // Update queue display
        updateQueueDisplay();
    }

    function togglePlayPause() {
        if (!audio.src || audio.src === window.location.href) {
            if (queue.length > 0) {
                playSongAtIndex(0);
            }
            return;
        }

        if (isPlaying) {
            audio.pause();
            isPlaying = false;
            setPlayIcon(btnPlay, false);
        } else {
            audio.play()
                .then(function () {
                    isPlaying = true;
                    setPlayIcon(btnPlay, true);
                })
                .catch(function (err) {
                    console.warn('Playback failed:', err.message);
                });
        }

        // Sync dedicated player page button
        var pageBtnPlay = document.getElementById('page-btn-play');
        setPlayIcon(pageBtnPlay, isPlaying);
    }

    function playNext() {
        if (queue.length === 0) return;

        if (isShuffle) {
            var randomIdx = Math.floor(Math.random() * queue.length);
            playSongAtIndex(randomIdx);
        } else {
            var nextIdx = currentIndex + 1;
            if (nextIdx >= queue.length) {
                if (repeatMode >= 1) {
                    nextIdx = 0; // wrap around
                } else {
                    return; // end of queue
                }
            }
            playSongAtIndex(nextIdx);
        }
    }

    function playPrev() {
        if (queue.length === 0) return;

        // If more than 3 seconds played, restart current song
        if (audio.currentTime > 3) {
            audio.currentTime = 0;
            return;
        }

        var prevIdx = currentIndex - 1;
        if (prevIdx < 0) {
            prevIdx = repeatMode >= 1 ? queue.length - 1 : 0;
        }
        playSongAtIndex(prevIdx);
    }

    // ========================= PROGRESS & SEEK =========================

    audio.addEventListener('timeupdate', function () {
        if (audio.duration && !isNaN(audio.duration)) {
            var percent = (audio.currentTime / audio.duration) * 100;
            if (progressFill) progressFill.style.width = percent + '%';
            if (timeCurrent) timeCurrent.textContent  = formatTime(audio.currentTime);
            if (timeTotal) timeTotal.textContent    = formatTime(audio.duration);

            // Sync dedicated player page progress
            var pageFill    = document.getElementById('page-progress-fill');
            var pageTimeCur = document.getElementById('page-time-current');
            var pageTimeTot = document.getElementById('page-time-total');
            if (pageFill)    pageFill.style.width    = percent + '%';
            if (pageTimeCur) pageTimeCur.textContent = formatTime(audio.currentTime);
            if (pageTimeTot) pageTimeTot.textContent = formatTime(audio.duration);
        }
    });

    // Seek on progress bar click
    if (progressContainer) {
        progressContainer.addEventListener('click', function (e) {
            if (!audio.duration || isNaN(audio.duration)) return;
            var rect    = progressContainer.getBoundingClientRect();
            var clickX  = e.clientX - rect.left;
            var percent = clickX / rect.width;
            audio.currentTime = percent * audio.duration;
        });
    }

    // When song ends, play next
    audio.addEventListener('ended', function () {
        if (repeatMode === 2) {
            audio.currentTime = 0;
            audio.play();
        } else {
            playNext();
        }
    });

    // ========================= VOLUME =========================

    (function initVolume() {
        var playerRight = document.querySelector('.player-right');
        if (!playerRight) return;

        var volumeBar  = playerRight.querySelector('.progress-bar-bg');
        var volumeFill = playerRight.querySelector('.progress-bar-fill');
        if (!volumeBar || !volumeFill) return;

        audio.volume = 1.0;
        volumeFill.style.width = '100%';

        volumeBar.addEventListener('click', function (e) {
            var rect    = volumeBar.getBoundingClientRect();
            var clickX  = e.clientX - rect.left;
            var percent = Math.max(0, Math.min(1, clickX / rect.width));
            audio.volume = percent;
            volumeFill.style.width = (percent * 100) + '%';

            var pageVolFill = document.getElementById('page-volume-fill');
            if (pageVolFill) pageVolFill.style.width = (percent * 100) + '%';
        });
    })();

    // ========================= CONTROLS EVENT LISTENERS =========================

    btnPlay.addEventListener('click', togglePlayPause);
    if (btnNext) btnNext.addEventListener('click', playNext);
    if (btnPrev) btnPrev.addEventListener('click', playPrev);

    if (btnShuffle) {
        btnShuffle.addEventListener('click', function () {
            isShuffle = !isShuffle;
            setShuffleIcon(btnShuffle, isShuffle);
            var pageShuffle = document.getElementById('page-btn-shuffle');
            setShuffleIcon(pageShuffle, isShuffle);
        });
    }

    if (btnRepeat) {
        btnRepeat.addEventListener('click', function () {
            repeatMode = (repeatMode + 1) % 3;
            setRepeatIcon(btnRepeat, repeatMode);
            var pageRepeat = document.getElementById('page-btn-repeat');
            setRepeatIcon(pageRepeat, repeatMode);
        });
    }

    // Favorite button click
    if (playerFav) {
        playerFav.addEventListener('click', toggleFavorite);
    }

    // ========================= SONG CARD CLICKS =========================

    function attachCardListeners() {
        var cards = document.querySelectorAll('.song-card');
        cards.forEach(function (card, index) {
            card.addEventListener('click', function () {
                buildQueueFromCards();
                playSongAtIndex(index);
            });
        });
    }

    function highlightActiveCard(songId) {
        var cards = document.querySelectorAll('.song-card');
        cards.forEach(function (card) {
            if (card.dataset.id === String(songId)) {
                card.classList.add('song-card--active');
            } else {
                card.classList.remove('song-card--active');
            }
        });
    }

    // ========================= PLAYLIST ROW CLICKS (Day 7) =========================

    function attachPlaylistRowListeners() {
        var rows = document.querySelectorAll('.playlist-song-row');
        if (rows.length === 0) return;

        rows.forEach(function (row, index) {
            row.addEventListener('click', function () {
                buildQueueFromPlaylistRows();
                playSongAtIndex(index);
            });
        });
    }

    function highlightActiveRow(songId) {
        var rows = document.querySelectorAll('.playlist-song-row');
        rows.forEach(function (row) {
            if (row.dataset.id === String(songId)) {
                row.classList.add('playlist-row--active');
            } else {
                row.classList.remove('playlist-row--active');
            }
        });
    }

    // ========================= KEYBOARD SHORTCUTS (Day 7) =========================

    document.addEventListener('keydown', function (e) {
        // Don't capture shortcuts when typing in inputs
        var tag = e.target.tagName.toLowerCase();
        if (tag === 'input' || tag === 'textarea' || tag === 'select') return;

        switch (e.code) {
            case 'Space':
                e.preventDefault();
                togglePlayPause();
                break;
            case 'ArrowRight':
                if (e.shiftKey) {
                    playNext();
                } else {
                    // Seek forward 5 seconds
                    if (audio.duration) audio.currentTime = Math.min(audio.currentTime + 5, audio.duration);
                }
                break;
            case 'ArrowLeft':
                if (e.shiftKey) {
                    playPrev();
                } else {
                    // Seek backward 5 seconds
                    audio.currentTime = Math.max(audio.currentTime - 5, 0);
                }
                break;
            case 'ArrowUp':
                e.preventDefault();
                audio.volume = Math.min(1, audio.volume + 0.1);
                syncVolumeUI();
                break;
            case 'ArrowDown':
                e.preventDefault();
                audio.volume = Math.max(0, audio.volume - 0.1);
                syncVolumeUI();
                break;
            case 'KeyM':
                // Mute/unmute toggle
                audio.muted = !audio.muted;
                syncVolumeUI();
                break;
            case 'KeyS':
                // Toggle shuffle
                if (btnShuffle) btnShuffle.click();
                break;
            case 'KeyR':
                // Cycle repeat
                if (btnRepeat) btnRepeat.click();
                break;
        }
    });

    function syncVolumeUI() {
        var vol = audio.muted ? 0 : audio.volume;
        var pct = (vol * 100) + '%';

        var playerRight = document.querySelector('.player-right');
        if (playerRight) {
            var gVolFill = playerRight.querySelector('.progress-bar-fill');
            if (gVolFill) gVolFill.style.width = pct;
        }
        var pageVolFill = document.getElementById('page-volume-fill');
        if (pageVolFill) pageVolFill.style.width = pct;
    }

    // ========================= QUEUE DISPLAY (Day 7) =========================

    function updateQueueDisplay() {
        var queueList = document.getElementById('queue-list');
        if (!queueList) return;

        queueList.innerHTML = '';

        if (queue.length === 0) {
            queueList.innerHTML = '<div class="queue-empty">No songs in queue</div>';
            return;
        }

        queue.forEach(function (song, idx) {
            var item = document.createElement('div');
            item.className = 'queue-item' + (idx === currentIndex ? ' queue-item--active' : '');
            item.innerHTML =
                '<span class="queue-num">' + (idx + 1) + '</span>' +
                '<div class="queue-info">' +
                '<div class="queue-song-title">' + escapeHtml(song.title) + '</div>' +
                '<div class="queue-song-artist">' + escapeHtml(song.artist) + '</div>' +
                '</div>';
            item.addEventListener('click', function () {
                playSongAtIndex(idx);
            });
            queueList.appendChild(item);
        });
    }

    function escapeHtml(text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // ========================= DEDICATED PLAYER PAGE SYNC =========================

    function updatePlayerPage(song) {
        var pageTitle  = document.getElementById('player-page-title');
        var pageArtist = document.getElementById('player-page-artist');
        var pageArt    = document.getElementById('player-page-art');

        if (pageTitle)  pageTitle.textContent  = song.title;
        if (pageArtist) pageArtist.textContent = song.artist;
        if (pageArt)    pageArt.src = song.cover || pageArt.src;
    }

    (function initPlayerPage() {
        var pageBtnPlay    = document.getElementById('page-btn-play');
        var pageBtnPrev    = document.getElementById('page-btn-prev');
        var pageBtnNext    = document.getElementById('page-btn-next');
        var pageBtnShuffle = document.getElementById('page-btn-shuffle');
        var pageBtnRepeat  = document.getElementById('page-btn-repeat');
        var pageProgressBg = document.getElementById('page-progress-container');
        var pageVolumeBar  = document.getElementById('page-volume-bar');

        if (pageBtnPlay)    pageBtnPlay.addEventListener('click', togglePlayPause);
        if (pageBtnPrev)    pageBtnPrev.addEventListener('click', playPrev);
        if (pageBtnNext)    pageBtnNext.addEventListener('click', playNext);

        if (pageBtnShuffle) {
            pageBtnShuffle.addEventListener('click', function () {
                if (btnShuffle) btnShuffle.click();
            });
        }

        if (pageBtnRepeat) {
            pageBtnRepeat.addEventListener('click', function () {
                if (btnRepeat) btnRepeat.click();
            });
        }

        if (pageProgressBg) {
            pageProgressBg.addEventListener('click', function (e) {
                if (!audio.duration || isNaN(audio.duration)) return;
                var rect    = pageProgressBg.getBoundingClientRect();
                var clickX  = e.clientX - rect.left;
                var percent = clickX / rect.width;
                audio.currentTime = percent * audio.duration;
            });
        }

        if (pageVolumeBar) {
            pageVolumeBar.addEventListener('click', function (e) {
                var rect    = pageVolumeBar.getBoundingClientRect();
                var clickX  = e.clientX - rect.left;
                var percent = Math.max(0, Math.min(1, clickX / rect.width));
                audio.volume = percent;

                var pageVolFill = document.getElementById('page-volume-fill');
                if (pageVolFill) pageVolFill.style.width = (percent * 100) + '%';

                var playerRight = document.querySelector('.player-right');
                if (playerRight) {
                    var gVolFill = playerRight.querySelector('.progress-bar-fill');
                    if (gVolFill) gVolFill.style.width = (percent * 100) + '%';
                }
            });
        }
    })();

    // ========================= INIT =========================

    buildQueueFromCards();
    attachCardListeners();
    attachPlaylistRowListeners();

})();
