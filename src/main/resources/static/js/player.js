/**
 * player.js — RevPlay HTML5 Audio Player
 *
 * Day 5: Wires up player-bar.html controls to the HTML5 <audio> element.
 * Features: play/pause, seek (progress bar click), volume, previous/next,
 *           song card click → play, now-playing display update.
 *
 * This script runs on every page via layout.html <script defer>.
 */
(function () {
    'use strict';

    // ========================= DOM REFERENCES =========================

    const audio        = document.getElementById('audio-element');
    const btnPlay      = document.getElementById('btn-play');
    const btnPrev      = document.getElementById('btn-prev');
    const btnNext      = document.getElementById('btn-next');
    const btnShuffle   = document.getElementById('btn-shuffle');
    const btnRepeat    = document.getElementById('btn-repeat');
    const progressContainer = document.getElementById('progress-container');
    const progressFill = document.getElementById('progress-fill');
    const timeCurrent  = document.getElementById('time-current');
    const timeTotal    = document.getElementById('time-total');
    const playerTitle  = document.getElementById('player-title');
    const playerArtist = document.getElementById('player-artist');
    const playerArt    = document.getElementById('player-art');
    const playerFav    = document.getElementById('player-favorite');

    // If the player-bar elements don't exist on this page, bail out
    if (!audio || !btnPlay) return;

    // ========================= PLAYER STATE =========================

    let queue         = [];   // Array of { id, url, title, artist, cover }
    let currentIndex  = -1;
    let isPlaying     = false;
    let isShuffle     = false;
    let repeatMode    = 0;    // 0 = off, 1 = repeat all, 2 = repeat one

    // ========================= UTILITY FUNCTIONS =========================

    /**
     * Format seconds into m:ss string
     */
    function formatTime(seconds) {
        if (isNaN(seconds) || seconds < 0) return '0:00';
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return mins + ':' + (secs < 10 ? '0' : '') + secs;
    }

    /**
     * Build the queue from all .song-card elements currently in the DOM.
     */
    function buildQueueFromCards() {
        const cards = document.querySelectorAll('.song-card');
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
     * player.js — Updated icon toggle functions
     * Supports image icons with emoji fallback
     */

// ========================= ICON HELPERS =========================

    /**
     * Toggle Play/Pause icon on a button.
     * Handles both image icons and emoji fallbacks.
     */
    function setPlayIcon(button, playing) {
        if (!button) return;

        // Find the image — could be .player-play-img or .page-play-icon
        var img = button.querySelector('.player-play-img')
            || button.querySelector('.page-play-icon');
        var fallback = button.querySelector('.play-fallback');

        if (img && img.style.display !== 'none') {
            // Image mode — swap src
            img.src = playing
                ? (img.dataset.pauseSrc || '/images/icons/pause.png')
                : (img.dataset.playSrc  || '/images/icons/play.png');
            img.alt = playing ? '⏸' : '▶';
        } else if (fallback) {
            // Fallback mode — swap emoji
            fallback.textContent = playing ? '⏸' : '▶';
        }

        button.title = playing ? 'Pause' : 'Play';
    }

    /**
     * Toggle Repeat icon on a button.
     * Modes: 0 = off, 1 = repeat all, 2 = repeat one
     */
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


    /**
     * Toggle Shuffle active state.
     */
    function setShuffleIcon(button, isActive) {
        if (!button) return;
        button.classList.toggle('active', isActive);
    }

    // ========================= CORE PLAYBACK =========================

    /**
     * Load and play a song by queue index.
     */
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
                    setPlayIcon(btnPlay, true);             // ← CHANGED
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

        // Also update the dedicated player page if we're on it
        updatePlayerPage(song);

        // Highlight active card
        highlightActiveCard(song.id);
    }

    /**
     * Toggle play / pause.
     */
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
            setPlayIcon(btnPlay, false);                    // ← CHANGED
        } else {
            audio.play()
                .then(function () {
                    isPlaying = true;
                    setPlayIcon(btnPlay, true);             // ← CHANGED
                })
                .catch(function (err) {
                    console.warn('Playback failed:', err.message);
                });
        }

        // Sync dedicated player page button
        var pageBtnPlay = document.getElementById('page-btn-play');
        setPlayIcon(pageBtnPlay, isPlaying);                // ← CHANGED
    }


    /**
     * Play next song in queue.
     */
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

    /**
     * Play previous song in queue.
     */
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

    /**
     * Update progress bar and time displays during playback.
     */
    audio.addEventListener('timeupdate', function () {
        if (audio.duration && !isNaN(audio.duration)) {
            var percent = (audio.currentTime / audio.duration) * 100;
            progressFill.style.width = percent + '%';
            timeCurrent.textContent  = formatTime(audio.currentTime);
            timeTotal.textContent    = formatTime(audio.duration);

            // Sync dedicated player page progress
            var pageFill    = document.getElementById('page-progress-fill');
            var pageTimeCur = document.getElementById('page-time-current');
            var pageTimeTot = document.getElementById('page-time-total');
            if (pageFill)    pageFill.style.width    = percent + '%';
            if (pageTimeCur) pageTimeCur.textContent = formatTime(audio.currentTime);
            if (pageTimeTot) pageTimeTot.textContent = formatTime(audio.duration);
        }
    });

    /**
     * Seek on progress bar click.
     */
    if (progressContainer) {
        progressContainer.addEventListener('click', function (e) {
            if (!audio.duration || isNaN(audio.duration)) return;
            var rect    = progressContainer.getBoundingClientRect();
            var clickX  = e.clientX - rect.left;
            var percent = clickX / rect.width;
            audio.currentTime = percent * audio.duration;
        });
    }

    /**
     * When song ends, play next.
     */
    audio.addEventListener('ended', function () {
        if (repeatMode === 2) {
            // Repeat one
            audio.currentTime = 0;
            audio.play();
        } else {
            playNext();
        }
    });

    // ========================= VOLUME =========================

    /**
     * Set up volume control in the player bar.
     * The volume bar is the .progress-bar-bg inside .player-right.
     */
    (function initVolume() {
        var playerRight = document.querySelector('.player-right');
        if (!playerRight) return;

        var volumeBar  = playerRight.querySelector('.progress-bar-bg');
        var volumeFill = playerRight.querySelector('.progress-bar-fill');
        if (!volumeBar || !volumeFill) return;

        // Set initial volume
        audio.volume = 1.0;
        volumeFill.style.width = '100%';

        volumeBar.addEventListener('click', function (e) {
            var rect    = volumeBar.getBoundingClientRect();
            var clickX  = e.clientX - rect.left;
            var percent = Math.max(0, Math.min(1, clickX / rect.width));
            audio.volume = percent;
            volumeFill.style.width = (percent * 100) + '%';

            // Sync page volume bar
            var pageVolFill = document.getElementById('page-volume-fill');
            if (pageVolFill) pageVolFill.style.width = (percent * 100) + '%';
        });
    })();

    // ========================= CONTROLS EVENT LISTENERS =========================

    btnPlay.addEventListener('click', togglePlayPause);
    if (btnNext)    btnNext.addEventListener('click', playNext);
    if (btnPrev)    btnPrev.addEventListener('click', playPrev);

    if (btnShuffle) {
        btnShuffle.addEventListener('click', function () {
            isShuffle = !isShuffle;
            setShuffleIcon(btnShuffle, isShuffle);          // ← CHANGED

            var pageShuffle = document.getElementById('page-btn-shuffle');
            setShuffleIcon(pageShuffle, isShuffle);          // ← CHANGED
        });
    }


    if (btnRepeat) {
        btnRepeat.addEventListener('click', function () {
            repeatMode = (repeatMode + 1) % 3;
            setRepeatIcon(btnRepeat, repeatMode);            // ← CHANGED

            var pageRepeat = document.getElementById('page-btn-repeat');
            setRepeatIcon(pageRepeat, repeatMode);           // ← CHANGED
        });
    }

    // ========================= SONG CARD CLICKS =========================

    /**
     * Attach click listeners to all song cards.
     * When a card is clicked, rebuild queue and play the clicked song.
     */
    function attachCardListeners() {
        var cards = document.querySelectorAll('.song-card');
        cards.forEach(function (card, index) {
            card.addEventListener('click', function () {
                buildQueueFromCards();
                playSongAtIndex(index);
            });
        });
    }

    /**
     * Highlight the currently playing card.
     */
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

    // ========================= DEDICATED PLAYER PAGE SYNC =========================

    /**
     * If on the /player page, update the hero section with current song info.
     */
    function updatePlayerPage(song) {
        var pageTitle  = document.getElementById('player-page-title');
        var pageArtist = document.getElementById('player-page-artist');
        var pageArt    = document.getElementById('player-page-art');

        if (pageTitle)  pageTitle.textContent  = song.title;
        if (pageArtist) pageArtist.textContent = song.artist;
        if (pageArt)    pageArt.src = song.cover || pageArt.src;
    }

    /**
     * Wire up the dedicated player page controls (if they exist).
     */
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
                btnShuffle.click(); // delegate to global button
            });
        }

        if (pageBtnRepeat) {
            pageBtnRepeat.addEventListener('click', function () {
                btnRepeat.click(); // delegate to global button
            });
        }

        // Seek on page progress bar click
        if (pageProgressBg) {
            pageProgressBg.addEventListener('click', function (e) {
                if (!audio.duration || isNaN(audio.duration)) return;
                var rect    = pageProgressBg.getBoundingClientRect();
                var clickX  = e.clientX - rect.left;
                var percent = clickX / rect.width;
                audio.currentTime = percent * audio.duration;
            });
        }

        // Volume on page volume bar click
        if (pageVolumeBar) {
            pageVolumeBar.addEventListener('click', function (e) {
                var rect    = pageVolumeBar.getBoundingClientRect();
                var clickX  = e.clientX - rect.left;
                var percent = Math.max(0, Math.min(1, clickX / rect.width));
                audio.volume = percent;

                var pageVolFill = document.getElementById('page-volume-fill');
                if (pageVolFill) pageVolFill.style.width = (percent * 100) + '%';

                // Sync global volume bar
                var playerRight = document.querySelector('.player-right');
                if (playerRight) {
                    var gVolFill = playerRight.querySelector('.progress-bar-fill');
                    if (gVolFill) gVolFill.style.width = (percent * 100) + '%';
                }
            });
        }
    })();

    // ========================= INIT =========================

    // Build initial queue and attach listeners on page load
    buildQueueFromCards();
    attachCardListeners();

})();
