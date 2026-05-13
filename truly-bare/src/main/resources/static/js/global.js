/* ============================================ */
/* TRULY BARE - GLOBAL INTERACTIONS            */
/* Search | Cart Sync | Notifications          */
/* ============================================ */

'use strict';

function toggleSearch() {
    const overlay = document.getElementById('searchOverlay');
    const dropdown = document.getElementById('searchResultsDropdown');
    const input = document.getElementById('searchInput');
    
    if (!overlay) return;

    overlay.classList.toggle('active');
    
    if (overlay.classList.contains('active')) {
        if (input) {
            input.value = '';
            input.focus();
        }
    } else {
        if (input) input.value = '';
        if (dropdown) dropdown.style.display = 'none';
    }
}

async function performSearch() {
    const input = document.getElementById('searchInput');
    const dropdown = document.getElementById('searchResultsDropdown');
    
    if (!input || !dropdown) return;
    
    const query = input.value.trim();
    
    if (query.length === 0) {
        dropdown.style.display = 'none';
        return;
    }
    
    try {
        const response = await fetch(`/api/products/search?query=${encodeURIComponent(query)}`);
        const products = await response.json();
        
        dropdown.innerHTML = '';
        if (products.length > 0) {
            products.forEach(p => {
                const item = document.createElement('a');
                item.href = `/product/${p.productId}`;
                item.className = 'search-result-item';
                item.style.cssText = 'display:flex; justify-content:space-between; padding:12px 15px; text-decoration:none; color:#1C1C1A; border-bottom:1px solid #f9f9f9; transition:background 0.2s;';
                
                item.onmouseover = () => item.style.background = '#fcfaf7';
                item.onmouseout = () => item.style.background = 'transparent';
                
                item.innerHTML = `
                    <span style="font-weight:500; font-size:13px; letter-spacing:0.5px;">${p.name}</span>
                    <span style="color:#8B1A1A; font-size:12px; font-weight:600;">₹${new Intl.NumberFormat().format(p.price)}</span>
                `;
                dropdown.appendChild(item);
                
                item.addEventListener('click', () => {
                    toggleSearch();
                });
            });
        } else {
            dropdown.innerHTML = '<div style="padding:15px; color:#999; font-size:12px; text-align:center; letter-spacing:1px;">No formulations found</div>';
        }
        dropdown.style.display = 'block';
    } catch (error) {
        console.error('Search failed:', error);
    }
}

// Close search on Escape key
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const overlay = document.getElementById('searchOverlay');
        if (overlay && overlay.classList.contains('active')) {
            toggleSearch();
        }
    }
});

// Initialize Cart Badge Globally
function updateGlobalCartBadge() {
    const badge = document.getElementById('cartBadge');
    if (!badge) return;
    try {
        const cart = JSON.parse(localStorage.getItem('trulyBareCart') || '[]');
        const total = cart.reduce((s, i) => s + (i.qty || 0), 0);
        badge.textContent = total;
        badge.style.display = total > 0 ? 'flex' : 'none';
    } catch(e) {
        console.warn('Cart badge sync failed');
    }
}

// Initialize Global Listeners
document.addEventListener('DOMContentLoaded', () => {
    updateGlobalCartBadge();
    
    // Global Logout Confirmation
    document.addEventListener('click', (e) => {
        const logoutLink = e.target.closest('a[href*="/logout"]');
        if (logoutLink) {
            e.preventDefault();
            if (typeof openTBConfirm === 'function') {
                openTBConfirm({
                    title: 'End Session?',
                    text: 'Are you sure you wish to end your session with Truly Bare? We look forward to your next visit.',
                    icon: 'bi-box-arrow-right',
                    confirmText: 'Sign Out',
                    onConfirm: () => { window.location.href = logoutLink.href; }
                });
            } else {
                // Fallback
                if (window.confirm('Are you sure you wish to end your session?')) {
                    window.location.href = logoutLink.href;
                }
            }
        }
    });
    // Global External Link Handler
    // Automatically adds target="_blank" and rel="noopener" to all external links
    const handleExternalLinks = () => {
        const links = document.querySelectorAll('a[href^="http"]');
        const currentHost = window.location.hostname;
        
        links.forEach(link => {
            try {
                const url = new URL(link.href);
                if (url.hostname !== currentHost) {
                    if (!link.hasAttribute('target')) {
                        link.setAttribute('target', '_blank');
                    }
                    if (!link.hasAttribute('rel')) {
                        link.setAttribute('rel', 'noopener noreferrer');
                    }
                }
            } catch (e) {
                // Invalid URL or relative protocol
            }
        });
    };
    
    handleExternalLinks();
    
    // Also watch for dynamically added links
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            if (mutation.addedNodes.length) {
                handleExternalLinks();
            }
        });
    });
    
    observer.observe(document.body, { childList: true, subtree: true });
});
