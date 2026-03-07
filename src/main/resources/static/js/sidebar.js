/**
 * sidebar.js — Mobile sidebar drawer toggle
 *
 * Hamburger button opens/closes the sidebar as a slide-out drawer.
 * Overlay click and link clicks close the sidebar.
 * Only active on mobile (≤768px) where the hamburger is visible.
 */
(function () {
    'use strict';

    var hamburger = document.getElementById('nav-hamburger');
    var sidebar = document.getElementById('sidebar');
    var overlay = document.getElementById('sidebar-overlay');

    if (!hamburger || !sidebar) return;

    function openSidebar() {
        sidebar.classList.add('sidebar--open');
        hamburger.classList.add('active');
        if (overlay) overlay.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    function closeSidebar() {
        sidebar.classList.remove('sidebar--open');
        hamburger.classList.remove('active');
        if (overlay) overlay.classList.remove('active');
        document.body.style.overflow = '';
    }

    function toggleSidebar() {
        if (sidebar.classList.contains('sidebar--open')) {
            closeSidebar();
        } else {
            openSidebar();
        }
    }

    // Hamburger click
    hamburger.addEventListener('click', function (e) {
        e.stopPropagation();
        toggleSidebar();
    });

    // Overlay click closes sidebar
    if (overlay) {
        overlay.addEventListener('click', closeSidebar);
    }

    // Clicking a sidebar link closes the sidebar (for mobile navigation)
    sidebar.addEventListener('click', function (e) {
        var link = e.target.closest('a');
        var btn = e.target.closest('button[type="submit"]'); // logout
        if (link || btn) {
            // Small delay so the navigation starts first
            setTimeout(closeSidebar, 100);
        }
    });

    // Close sidebar on Escape key
    document.addEventListener('keydown', function (e) {
        if (e.code === 'Escape' && sidebar.classList.contains('sidebar--open')) {
            closeSidebar();
        }
    });

    // Close sidebar on window resize if we go above mobile breakpoint
    window.addEventListener('resize', function () {
        if (window.innerWidth > 768 && sidebar.classList.contains('sidebar--open')) {
            closeSidebar();
        }
    });

    // Close sidebar after PJAX navigation
    document.addEventListener('pjax:complete', closeSidebar);

    // Expose for other scripts
    window.SidebarDrawer = {
        open: openSidebar,
        close: closeSidebar,
        toggle: toggleSidebar
    };

})();