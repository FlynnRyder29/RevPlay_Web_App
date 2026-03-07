/* ═══════════════════════════════════════════════════════
   CONFIRM & TOAST — Global UI Notification System
   RevPlay.confirm()  → themed confirmation dialog (Promise)
   RevPlay.toast()    → auto-dismiss toast notifications

   IMPORTANT: Uses Object.defineProperty to prevent other
   scripts from accidentally overwriting these methods.
   ═══════════════════════════════════════════════════════ */

(function () {
    'use strict';

    // ══════════════════════════════════════════════════════════
    // TOAST SYSTEM
    // ══════════════════════════════════════════════════════════

    var toastContainer = null;
    var MAX_TOASTS = 5;

    function getToastContainer() {
        if (toastContainer && document.body.contains(toastContainer)) return toastContainer;
        toastContainer = document.getElementById('revplay-toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'revplay-toast-container';
            toastContainer.className = 'rp-toast-container';
            document.body.appendChild(toastContainer);
        }
        return toastContainer;
    }

    var TOAST_ICONS = {
        success: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>',
        error: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
        warning: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
        info: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>'
    };

    function showToast(opts) {
        if (!opts || !opts.message) return;

        var type = opts.type || 'info';
        var duration = opts.duration !== undefined ? opts.duration : 3500;
        var container = getToastContainer();

        var existing = container.querySelectorAll('.rp-toast');
        if (existing.length >= MAX_TOASTS) {
            dismissToast(existing[0]);
        }

        var toast = document.createElement('div');
        toast.className = 'rp-toast rp-toast--' + type;
        toast.setAttribute('role', 'alert');

        var iconHtml = TOAST_ICONS[type] || TOAST_ICONS.info;
        var titleHtml = opts.title ? '<div class="rp-toast-title">' + escapeHtml(opts.title) + '</div>' : '';

        toast.innerHTML =
            '<div class="rp-toast-icon">' + iconHtml + '</div>' +
            '<div class="rp-toast-body">' +
            titleHtml +
            '<div class="rp-toast-message">' + escapeHtml(opts.message) + '</div>' +
            '</div>' +
            '<button class="rp-toast-close" aria-label="Close">' +
            '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>' +
            '</button>' +
            '<div class="rp-toast-progress"><div class="rp-toast-progress-bar rp-toast-progress--' + type + '"></div></div>';

        toast.querySelector('.rp-toast-close').addEventListener('click', function () {
            dismissToast(toast);
        });

        container.appendChild(toast);

        requestAnimationFrame(function () {
            toast.classList.add('rp-toast--visible');
        });

        if (duration > 0) {
            var progressBar = toast.querySelector('.rp-toast-progress-bar');
            if (progressBar) {
                progressBar.style.transition = 'width ' + duration + 'ms linear';
                requestAnimationFrame(function () {
                    progressBar.style.width = '0%';
                });
            }

            var timer = setTimeout(function () {
                dismissToast(toast);
            }, duration);

            toast.addEventListener('mouseenter', function () {
                clearTimeout(timer);
                if (progressBar) {
                    var computed = getComputedStyle(progressBar);
                    progressBar.style.transition = 'none';
                    progressBar.style.width = computed.width;
                }
            });

            toast.addEventListener('mouseleave', function () {
                if (progressBar) {
                    var currentWidth = parseFloat(getComputedStyle(progressBar).width);
                    var totalWidth = parseFloat(getComputedStyle(progressBar.parentElement).width);
                    var remaining = totalWidth > 0 ? (currentWidth / totalWidth) * duration : 1000;
                    remaining = Math.max(remaining, 500);

                    progressBar.style.transition = 'width ' + remaining + 'ms linear';
                    requestAnimationFrame(function () {
                        progressBar.style.width = '0%';
                    });

                    timer = setTimeout(function () {
                        dismissToast(toast);
                    }, remaining);
                }
            });
        }

        return toast;
    }

    function dismissToast(toast) {
        if (!toast || toast._dismissing) return;
        toast._dismissing = true;
        toast.classList.remove('rp-toast--visible');
        toast.classList.add('rp-toast--exit');
        setTimeout(function () {
            if (toast.parentNode) toast.parentNode.removeChild(toast);
        }, 350);
    }


    // ══════════════════════════════════════════════════════════
    // CONFIRM DIALOG
    // ══════════════════════════════════════════════════════════

    var confirmOverlay = null;
    var resolveFunc = null;

    function getConfirmModal() {
        if (confirmOverlay && document.body.contains(confirmOverlay)) return confirmOverlay;
        confirmOverlay = document.getElementById('revplay-confirm-overlay');
        if (!confirmOverlay) {
            confirmOverlay = document.createElement('div');
            confirmOverlay.id = 'revplay-confirm-overlay';
            confirmOverlay.className = 'rp-confirm-overlay';
            confirmOverlay.innerHTML =
                '<div class="rp-confirm-modal">' +
                '  <div class="rp-confirm-icon" id="rp-confirm-icon"></div>' +
                '  <h3 class="rp-confirm-title" id="rp-confirm-title">Are you sure?</h3>' +
                '  <p class="rp-confirm-message" id="rp-confirm-message"></p>' +
                '  <div class="rp-confirm-actions">' +
                '    <button class="rp-confirm-btn rp-confirm-btn--cancel" id="rp-confirm-cancel">Cancel</button>' +
                '    <button class="rp-confirm-btn rp-confirm-btn--ok" id="rp-confirm-ok">Confirm</button>' +
                '  </div>' +
                '</div>';
            document.body.appendChild(confirmOverlay);

            confirmOverlay.addEventListener('click', function (e) {
                if (e.target === confirmOverlay) closeConfirm(false);
            });

            document.getElementById('rp-confirm-cancel').addEventListener('click', function () {
                closeConfirm(false);
            });

            document.getElementById('rp-confirm-ok').addEventListener('click', function () {
                closeConfirm(true);
            });

            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape' && confirmOverlay.classList.contains('rp-confirm--visible')) {
                    closeConfirm(false);
                }
            });
        }
        return confirmOverlay;
    }

    var CONFIRM_ICONS = {
        danger: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round"><path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>',
        warning: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
        info: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>',
        success: '<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>'
    };

    function showConfirm(opts) {
        if (!opts || !opts.message) return Promise.resolve(false);

        var type = opts.type || 'danger';
        var modal = getConfirmModal();

        document.getElementById('rp-confirm-icon').innerHTML = CONFIRM_ICONS[type] || CONFIRM_ICONS.danger;
        document.getElementById('rp-confirm-title').textContent = opts.title || 'Are you sure?';
        document.getElementById('rp-confirm-message').textContent = opts.message;

        var okBtn = document.getElementById('rp-confirm-ok');
        var cancelBtn = document.getElementById('rp-confirm-cancel');

        okBtn.textContent = opts.confirmText || 'Confirm';
        cancelBtn.textContent = opts.cancelText || 'Cancel';

        okBtn.className = 'rp-confirm-btn rp-confirm-btn--ok rp-confirm-btn--' + type;

        modal.classList.add('rp-confirm--visible');
        okBtn.focus();

        return new Promise(function (resolve) {
            resolveFunc = resolve;
        });
    }

    function closeConfirm(result) {
        var modal = getConfirmModal();
        modal.classList.remove('rp-confirm--visible');
        if (resolveFunc) {
            var fn = resolveFunc;
            resolveFunc = null;
            fn(result);
        }
    }

    function escapeHtml(text) {
        if (!text) return '';
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }


    // ══════════════════════════════════════════════════════════
    // EXPOSE — Intercept window.RevPlay assignment
    //
    // Problem: player.js does `window.RevPlay = { playQueue, ... }`
    // which DESTROYS .confirm and .toast.
    //
    // Solution: Store our functions, then use a setter trap on
    // window.RevPlay so that when player.js overwrites the object,
    // we merge our functions back in.
    // ══════════════════════════════════════════════════════════

    var _confirmFn = showConfirm;
    var _toastFn = showToast;

    // Also store as independent globals that can never be overwritten
    window.__rpConfirm = showConfirm;
    window.__rpToast = showToast;

    // Initial assignment
    var _revplay = window.RevPlay || {};
    _revplay.confirm = _confirmFn;
    _revplay.toast = _toastFn;

    try {
        var _currentRevPlay = _revplay;

        Object.defineProperty(window, 'RevPlay', {
            get: function () {
                return _currentRevPlay;
            },
            set: function (newVal) {
                // When player.js (or any script) does window.RevPlay = {...}
                // merge our confirm/toast into the new object
                if (newVal && typeof newVal === 'object') {
                    newVal.confirm = _confirmFn;
                    newVal.toast = _toastFn;
                    _currentRevPlay = newVal;
                }
            },
            configurable: true,
            enumerable: true
        });
    } catch (e) {
        // Fallback if defineProperty fails (shouldn't happen in modern browsers)
        window.RevPlay = _revplay;
    }

})();