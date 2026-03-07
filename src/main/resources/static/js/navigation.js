/**
 * navigation.js — PJAX Router for Continuous Playback
 *
 * Intercepts internal link clicks, fetches the next page via AJAX,
 * and replaces only the .content-area, keeping the audio player intact.
 *
 * Day 9 fixes:
 *   - Calls RevPlay.rescan() after every content swap
 *   - Dispatches 'pjax:complete' for per-page scripts
 *   - Exposes PjaxRouter.invalidateCache / .reload / .navigate
 *   - Stores initial popstate
 * Day 10 fixes:
 *   - Navbar search uses PJAX instead of form submit
 *   - Form interception for any remaining forms
 */
(function () {
    'use strict';

    var ENABLE_PJAX = true;
    var CONTENT_SELECTOR = '.content-area';
    var TITLE_SELECTOR = 'title';

    if (!ENABLE_PJAX || !window.history || !window.history.pushState) return;

    var contentArea = document.querySelector(CONTENT_SELECTOR);
    if (!contentArea) return;

    var pageCache = {};

    function isInternalLink(link) {
        if (link.tagName !== 'A' || !link.href) return false;
        if (link.host !== window.location.host) return false;
        if (link.target === '_blank') return false;
        if (link.hasAttribute('download')) return false;

        var path = link.pathname;
        if (path.startsWith('/auth/logout') || path.startsWith('/oauth2')) return false;
        if (link.closest('#admin-btn')) return false;

        return true;
    }

    function showLoader() {
        contentArea.style.opacity = '0.5';
        contentArea.style.pointerEvents = 'none';
    }

    function hideLoader() {
        contentArea.style.opacity = '1';
        contentArea.style.pointerEvents = 'auto';
    }

    function executeScripts(container) {
        var scripts = container.querySelectorAll('script');
        scripts.forEach(function (oldScript) {
            var newScript = document.createElement('script');
            Array.from(oldScript.attributes).forEach(function (attr) {
                newScript.setAttribute(attr.name, attr.value);
            });
            newScript.textContent = oldScript.textContent;
            oldScript.parentNode.replaceChild(newScript, oldScript);
        });
    }

    function reinitComponents() {
        if (window.RevPlay && window.RevPlay.rescan) {
            window.RevPlay.rescan();
        }

        if (typeof window.initFavorites === 'function') {
            window.initFavorites();
        }

        document.dispatchEvent(new CustomEvent('pjax:complete'));
    }

    function loadPage(url, pushHistory) {
        if (url === window.location.href && pushHistory) return;

        showLoader();

        if (pageCache[url]) {
            renderContent(pageCache[url].html, pageCache[url].title, url, pushHistory);
            hideLoader();
            return;
        }

        fetch(url, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            credentials: 'same-origin'
        })
            .then(function (res) {
                if (res.redirected) {
                    window.location.href = res.url;
                    throw new Error('Redirected');
                }
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.text();
            })
            .then(function (html) {
                var parser = new DOMParser();
                var doc = parser.parseFromString(html, 'text/html');

                var newContent = doc.querySelector(CONTENT_SELECTOR);
                var newTitle = doc.querySelector(TITLE_SELECTOR);

                if (!newContent) {
                    window.location.href = url;
                    return;
                }

                var titleText = newTitle ? newTitle.textContent : document.title;
                var newHtml = newContent.innerHTML;

                pageCache[url] = { html: newHtml, title: titleText };

                renderContent(newHtml, titleText, url, pushHistory);
                hideLoader();
            })
            .catch(function (err) {
                if (err.message !== 'Redirected') {
                    console.error('PJAX Error:', err);
                    hideLoader();
                    window.location.href = url;
                }
            });
    }

    function renderContent(html, title, url, pushHistory) {
        document.title = title;
        contentArea.innerHTML = html;

        if (pushHistory) {
            window.history.pushState({ url: url }, title, url);
        }

        executeScripts(contentArea);
        reinitComponents();

        window.scrollTo(0, 0);
        updateSidebarActiveLink(url);
    }

    function updateSidebarActiveLink(url) {
        var path = new URL(url, window.location.origin).pathname;
        var links = document.querySelectorAll('.sidebar-item');

        links.forEach(function (link) {
            if (link.getAttribute('href') === path) {
                link.classList.add('sidebar-item--active');
            } else {
                link.classList.remove('sidebar-item--active');
            }
        });
    }

    function invalidateCache(pattern) {
        if (!pattern) {
            pageCache = {};
            return;
        }

        Object.keys(pageCache).forEach(function (url) {
            var shouldInvalidate = false;
            if (typeof pattern === 'string') {
                shouldInvalidate = url.indexOf(pattern) !== -1;
            } else if (pattern instanceof RegExp) {
                shouldInvalidate = pattern.test(url);
            }
            if (shouldInvalidate) {
                delete pageCache[url];
            }
        });
    }

    // ── Link click listener ──
    document.addEventListener('click', function (e) {
        var a = e.target.closest('a');
        if (!a) return;
        if (e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;

        if (isInternalLink(a)) {
            e.preventDefault();
            loadPage(a.href, true);
        }
    });

    // ── Form submission interceptor ──
    // Catches ALL GET forms that point to internal URLs and routes them through PJAX
    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!form || form.tagName !== 'FORM') return;

        // Only intercept GET forms (search, filter, etc.)
        var method = (form.method || 'get').toLowerCase();
        if (method !== 'get') return;

        // Only intercept forms pointing to same host
        var action = form.action || window.location.href;
        try {
            var actionUrl = new URL(action, window.location.origin);
            if (actionUrl.host !== window.location.host) return;
        } catch (err) {
            return;
        }

        // Skip auth-related forms
        var path = new URL(action, window.location.origin).pathname;
        if (path.startsWith('/auth/') || path.startsWith('/oauth2')) return;

        e.preventDefault();

        // Build URL from form data
        var formData = new FormData(form);
        var params = new URLSearchParams();
        formData.forEach(function (value, key) {
            if (value) params.append(key, value);
        });

        var url = path + (params.toString() ? '?' + params.toString() : '');
        loadPage(url, true);

    }, true);

    // ── Navbar search handler ──
    // The navbar is OUTSIDE .content-area so it's never replaced by PJAX.
    // We bind once here and it persists for the entire session.
    (function initNavbarSearch() {
        var navInput = document.getElementById('nav-search-input');
        if (!navInput) return;

        navInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.keyCode === 13) {
                e.preventDefault();
                var query = navInput.value.trim();
                if (!query) return;

                var url = '/library?q=' + encodeURIComponent(query);
                loadPage(url, true);
            }
        });
    })();

    // ── Back/forward ──
    window.addEventListener('popstate', function (e) {
        if (e.state && e.state.url) {
            loadPage(e.state.url, false);
        } else {
            loadPage(window.location.href, false);
        }
    });

    // Store initial state
    window.history.replaceState({ url: window.location.href }, document.title, window.location.href);

    // ── Public API ──
    window.PjaxRouter = {
        invalidateCache: invalidateCache,
        navigate: function (url) { loadPage(url, true); },
        reload: function () {
            var url = window.location.href;
            delete pageCache[url];
            loadPage(url, false);
        }
    };

})();