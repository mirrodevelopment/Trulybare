/* ============================================ */
/* TRULY BARE - LUXURY CAROUSEL & INTERACTIONS  */
/* Aesop-Inspired | Elegant | Minimal           */
/* ============================================ */

'use strict';

// ============================================
// GLOBAL VARIABLES
// ============================================
let trulySwiper = null;
const CONFIG = {
    autoplayDelay: 5000,
    transitionSpeed: 600,
    mobileBreakpoint: 639,
    tabletBreakpoint: 1023
};

// ============================================
// INITIALIZATION
// ============================================
document.addEventListener('DOMContentLoaded', function () {
    // Global swipe detection for mobile popups
    window.isSwiping = false;
    document.addEventListener('touchstart', () => window.isSwiping = false, { passive: true });
    document.addEventListener('touchmove', () => window.isSwiping = true, { passive: true });

    initMobileMenu();
    initSmoothScroll();
    initHoverEffects();
    initNewsletterForm();
    initScrollHeader();
    initVlogSection();
    initStoryTimeline();
    initQuickView();
    initAntiGravityCarousel();
    initHeroSwiper();
    logInitialization();
});



// ============================================
// UPDATE ACTIVE SLIDE
// ============================================
function updateActiveSlide() {
    const activeSlide = document.querySelector('.swiper-slide-active');
    if (activeSlide) {
        activeSlide.setAttribute('aria-current', 'true');
    }
}

// ============================================
// SCROLL HEADER
// ============================================
function initScrollHeader() {
    const header = document.querySelector('.header');
    if (!header) return;

    let lastScrollY = window.scrollY;
    const THRESHOLD = 60; // px from top before hide logic kicks in

    window.addEventListener('scroll', () => {
        const currentScrollY = window.scrollY;

        // Toggle scrolled style (glassmorphism enhancement)
        header.classList.toggle('scrolled', currentScrollY > 30);

        if (currentScrollY <= THRESHOLD) {
            // Near top — always show
            header.classList.remove('header-hidden');
        } else if (currentScrollY > lastScrollY) {
            // Scrolling DOWN — hide
            header.classList.add('header-hidden');
        } else {
            // Scrolling UP — reveal
            header.classList.remove('header-hidden');
        }

        lastScrollY = currentScrollY;
    }, { passive: true });
}

// ============================================
// MOBILE MENU
// ============================================
function initMobileMenu() {
    const menuBtn = document.getElementById('mobileMenuBtn');
    const mobileNav = document.getElementById('mobileNav');
    const closeBtn = document.getElementById('mobileNavClose');
    const backdrop = document.getElementById('mobileNavBackdrop');
    
    if (!menuBtn || !mobileNav) return;

    let isOpen = false;

    function openNav() {
        isOpen = true;
        mobileNav.classList.add('open');
        mobileNav.setAttribute('aria-hidden', 'false');
        menuBtn.setAttribute('aria-expanded', 'true');
        menuBtn.classList.add('is-open');
        document.body.style.overflow = 'hidden';
        document.documentElement.style.overflow = 'hidden';
    }

    function closeNav() {
        isOpen = false;
        mobileNav.classList.remove('open');
        mobileNav.setAttribute('aria-hidden', 'true');
        menuBtn.setAttribute('aria-expanded', 'false');
        menuBtn.classList.remove('is-open');
        document.body.style.overflow = '';
        document.documentElement.style.overflow = '';
    }

    // Toggle on hamburger button click
    menuBtn.addEventListener('click', function (e) {
        e.stopPropagation();
        isOpen ? closeNav() : openNav();
    });

    // Close on X button
    if (closeBtn) {
        closeBtn.addEventListener('click', closeNav);
    }

    // Close on backdrop click
    if (backdrop) {
        backdrop.addEventListener('click', closeNav);
    }

    // Nav links: close drawer, then handle navigation
    mobileNav.querySelectorAll('.mobile-menu-list a').forEach(function (link) {
        link.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            closeNav();

            if (!href || href === '#' || href === 'javascript:void(0)') {
                e.preventDefault();
                return;
            }

            // Same-page anchor → let initSmoothScroll handle it
            if (href.startsWith('#')) {
                return;
            }

            // External/Full URL → navigate after close animation
            e.preventDefault();
            setTimeout(function () {
                window.location.href = href;
            }, 400);
        });
    });

    // Close on ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && isOpen) closeNav();
    });
}

// ============================================
// SMOOTH SCROLL + ACTIVE NAV
// ============================================
function initSmoothScroll() {
    const header = document.getElementById('header');

    function scrollToSection(href) {
        if (!href || href === '#') return;
        const target = document.querySelector(href);
        if (!target) return;
        const headerH = header ? header.offsetHeight + 12 : 80;
        const top = target.getBoundingClientRect().top + window.scrollY - headerH;
        window.scrollTo({ top, behavior: 'smooth' });
    }

    // Wire up all anchor links on this page
    document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href === '#') return;
            const target = document.querySelector(href);
            if (target) {
                e.preventDefault();
                scrollToSection(href);
                // Update URL hash without jumping
                history.pushState(null, '', href);
            }
        });
    });

    // If page loaded with a hash (e.g. /home#shop from cart link), scroll to it
    if (window.location.hash) {
        setTimeout(function () {
            scrollToSection(window.location.hash);
        }, 300);
    }

    // Active nav link highlight via IntersectionObserver
    const sections = document.querySelectorAll('section[id], div[id]');
    const navLinks = document.querySelectorAll('.header-nav a[href^="#"]');

    if (navLinks.length && sections.length) {
        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    const id = entry.target.getAttribute('id');
                    navLinks.forEach(function (link) {
                        link.classList.toggle('active', link.getAttribute('href') === '#' + id);
                    });
                }
            });
        }, { rootMargin: '-20% 0px -60% 0px', threshold: 0 });

        sections.forEach(function (s) { observer.observe(s); });
    }
}

// ============================================
// HOVER EFFECTS
// ============================================
function initHoverEffects() {
    const carousel = document.querySelector('.card-carousel');
    if (!carousel) return;

    carousel.addEventListener('mouseenter', function () {
        if (trulySwiper && trulySwiper.autoplay) {
            trulySwiper.autoplay.stop();
        }
    });

    carousel.addEventListener('mouseleave', function () {
        if (trulySwiper && trulySwiper.autoplay) {
            trulySwiper.autoplay.start();
        }
    });
}

// ============================================
// NEWSLETTER FORM
// ============================================
function initNewsletterForm() {
    const form = document.querySelector('.newsletter-form');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        const email = this.querySelector('input[type="email"]').value;

        if (email) {
            showToast('Thank you for subscribing.');
            this.reset();
        }
    });
}

// ============================================
// TOAST NOTIFICATION
// ============================================
function showToast(message) {
    const existingToast = document.querySelector('.toast-notification');
    if (existingToast) existingToast.remove();

    const toast = document.createElement('div');
    toast.className = 'toast-notification';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        bottom: 40px;
        left: 50%;
        transform: translateX(-50%);
        background: var(--text-dark, #2A2A28);
        color: white;
        padding: 12px 32px;
        font-size: 13px;
        font-weight: 300;
        letter-spacing: 2px;
        text-transform: uppercase;
        z-index: 1000;
        animation: fadeInOut 2.5s ease;
        box-shadow: 0 4px 20px rgba(0,0,0,0.08);
        white-space: nowrap;
    `;

    document.body.appendChild(toast);

    setTimeout(() => {
        if (toast.parentNode) toast.remove();
    }, 2500);
}

// ============================================
// RESIZE HANDLER
// ============================================
let resizeTimer;
window.addEventListener('resize', function () {
    clearTimeout(resizeTimer);

    resizeTimer = setTimeout(function () {
        if (trulySwiper) {
            trulySwiper.update();
        }
    }, 250);
});

// ============================================
// LOGGING
// ============================================
function logInitialization() {
    const width = window.innerWidth;
    let device = 'Desktop';
    if (width <= CONFIG.mobileBreakpoint) device = 'Mobile';
    else if (width <= CONFIG.tabletBreakpoint) device = 'Tablet';

    console.log(`🌿 Truly Bare | ${device} | ${width}px`);
}

// ============================================
// ANIMATION STYLES
// ============================================
const style = document.createElement('style');
style.textContent = `
    @keyframes fadeInOut {
        0% { opacity: 0; transform: translateX(-50%) translateY(10px); }
        10% { opacity: 1; transform: translateX(-50%) translateY(0); }
        90% { opacity: 1; transform: translateX(-50%) translateY(0); }
        100% { opacity: 0; transform: translateX(-50%) translateY(-10px); }
    }
    
    @keyframes subtleFade {
        from { opacity: 0; }
        to { opacity: 1; }
    }
    
    .carousel-card {
        animation: subtleFade 0.6s ease-out;
    }
`;
document.head.appendChild(style);

// ============================================
// ADD TO CART
// ============================================
/**
 * Called by every "Add to Cart" button on the home page.
 * 1. Reads product name and price from the nearest card/article.
 * 2. Saves the item to localStorage (cart array) so cart.html can pick it up.
 * 3. Updates the header cart badge count.
 * 4. Shows a short success toast, then redirects to /cart.
 */
function addToCart(btn, price) {
    console.log('🛒 Truly Bare | Adding to cart...', { price });

    // Find the product container — works on home, products, and product-detail pages
    const card = btn.closest('article, .product-card, .carousel-card, .swiper-slide, .product-actions, .product-detail-info');

    // Extract product name — from card child element, or from the page h1, or fallback
    let name = 'Premium formulation';
    let img = '';
    let id = Math.floor(Math.random() * 1000);

    if (card) {
        name = (card.querySelector('.card-title, .product-name, h3, h4') || {}).textContent?.trim() || name;
        img  = (card.querySelector('img') || {}).src || '';
        id   = card.dataset.productId || id;
    }

    // Fallback for product-detail page: read name from the page <h1> and id from URL
    if (!card || card.classList.contains('product-actions') || card.classList.contains('product-detail-info')) {
        const pageH1 = document.querySelector('h1.product-name');
        if (pageH1) name = pageH1.textContent.trim();
        const pageImg = document.querySelector('.product-detail-media img');
        if (pageImg) img = pageImg.src;
        const productActions = document.querySelector('.product-actions[data-product-id]');
        if (productActions) id = productActions.dataset.productId || id;
    }

    // Load and update cart
    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem('trulyBareCart') || '[]');
    } catch (e) {
        console.warn('⚠️ localStorage cart corrupted, resetting');
        cart = [];
    }

    const existing = cart.find(function (i) { return String(i.id) === String(id); });
    if (existing) {
        existing.qty += 1;
    } else {
        cart.push({ id: id, name: name, price: price, qty: 1, image: img });
    }

    // Save back
    localStorage.setItem('trulyBareCart', JSON.stringify(cart));

    // Update UI Badge
    if (typeof updateGlobalCartBadge === 'function') {
        updateGlobalCartBadge();
        const badge = document.getElementById('cartBadge');
        if (badge) {
            badge.classList.add('pulse');
            setTimeout(() => badge.classList.remove('pulse'), 500);
        }
    }

    // Button Feedback
    const originalHtml = btn.innerHTML;
    btn.classList.add('added');
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-check2"></i> Added';

    // Toast — stay on page
    showToast(`✓ ${name} added to cart`);

    // Reset button after 2s so user can add more
    setTimeout(function () {
        btn.classList.remove('added');
        btn.disabled = false;
        btn.innerHTML = originalHtml;
    }, 2000);
}

// ============================================
// EXPORT
// ============================================
window.TrulyBare = {
    getSwiper: () => trulySwiper,
    showToast,
    addToCart,
    version: '2.0.0'
};

// ============================================
// HERO SWIPER
// ============================================
function initHeroSwiper() {
    const heroEl = document.querySelector('.hero-swiper');
    if (!heroEl) return;

    trulySwiper = new Swiper('.hero-swiper', {
        loop: true,
        effect: 'fade',
        fadeEffect: { crossFade: true },
        speed: 1000,
        autoplay: {
            delay: 6000,
            disableOnInteraction: false,
        },
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        on: {
            slideChangeTransitionStart: function () {
                const content = this.slides[this.activeIndex].querySelector('.hero-content');
                if (content) {
                    content.style.opacity = '0';
                    content.style.transform = 'translateY(20px)';
                }
            },
            slideChangeTransitionEnd: function () {
                const content = this.slides[this.activeIndex].querySelector('.hero-content');
                if (content) {
                    content.style.opacity = '1';
                    content.style.transform = 'translateY(0)';
                    content.style.transition = 'all 0.8s ease-out';
                }
            }
        }
    });
}

// ============================================
// VLOG + COMMUNITY SECTION
// ============================================
function initVlogSection() {

    /* ---- Generic slider factory ---- */
    function makeSlider(slidesId, dotsId, prevId, nextId, counterId, autoMs) {
        const slidesEl = document.getElementById(slidesId);
        const dotsEl = document.getElementById(dotsId);
        const prevBtn = prevId ? document.getElementById(prevId) : null;
        const nextBtn = nextId ? document.getElementById(nextId) : null;
        const counterEl = counterId ? document.getElementById(counterId) : null;
        if (!slidesEl) return;

        const slides = slidesEl.querySelectorAll(
            slidesId === 'vlogSlides' ? '.vlog-slide' : '.comment-slide'
        );
        const dots = dotsEl ? dotsEl.querySelectorAll('.vlog-dot') : [];
        const total = slides.length;
        let current = 0;
        let timer = null;

        function goTo(idx) {
            slides[current].classList.remove('active');
            if (dots[current]) dots[current].classList.remove('active');
            current = (idx + total) % total;
            slides[current].classList.add('active');
            if (dots[current]) dots[current].classList.add('active');
            if (counterEl) counterEl.textContent = (current + 1) + ' / ' + total;
        }

        function next() { goTo(current + 1); }
        function prev() { goTo(current - 1); }

        function startAuto() {
            stopAuto();
            timer = setInterval(next, autoMs);
        }
        function stopAuto() {
            if (timer) { clearInterval(timer); timer = null; }
        }

        if (prevBtn) prevBtn.addEventListener('click', () => { prev(); startAuto(); });
        if (nextBtn) nextBtn.addEventListener('click', () => { next(); startAuto(); });

        dots.forEach((dot, i) => {
            dot.addEventListener('click', () => { goTo(i); startAuto(); });
        });

        // Pause on hover
        if (slidesEl.parentElement) {
            slidesEl.parentElement.addEventListener('mouseenter', stopAuto);
            slidesEl.parentElement.addEventListener('mouseleave', startAuto);
        }

        startAuto();
    }

    // Vlogs auto-rotate every 6 seconds
    makeSlider('vlogSlides', 'vlogDots', 'vlogPrev', 'vlogNext', 'vlogCounter', 6000);
    // Comments auto-rotate every 7 seconds (offset from vlogs)
    setTimeout(() => {
        makeSlider('commentSlides', 'commentDots', null, null, null, 7000);
    }, 1500);

    // Play button overlay — click plays video and hides overlay
    document.querySelectorAll('.vlog-thumb').forEach(thumb => {
        const video = thumb.querySelector('.vlog-video');
        const overlay = thumb.querySelector('.vlog-play-overlay');
        const playBtn = thumb.querySelector('.vlog-play-btn');

        if (!video || !overlay || !playBtn) return;

        playBtn.addEventListener('click', () => {
            video.play();
            overlay.classList.add('hidden');
        });

        // Show overlay again when video pauses or ends
        video.addEventListener('pause', () => overlay.classList.remove('hidden'));
        video.addEventListener('ended', () => overlay.classList.remove('hidden'));

        // Native controls click also hides overlay
        video.addEventListener('play', () => overlay.classList.add('hidden'));
    });
}

// ============================================
// STORY TIMELINE MODAL
// ============================================
function initStoryTimeline() {
    const openBtn = document.getElementById('openStoryModal');
    const modal = document.getElementById('storyModal');
    const closeBtn = document.getElementById('closeStoryModalBtn');
    if (!openBtn || !modal) return;

    function openModal() {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        document.documentElement.style.overflow = 'hidden';

        // Trigger animations for items currently in view
        setTimeout(checkTimelineScroll, 300);
    }

    function closeModal() {
        modal.classList.remove('active');
        document.body.style.overflow = '';
        document.documentElement.style.overflow = '';
    }

    openBtn.addEventListener('click', openModal);
    if (closeBtn) closeBtn.addEventListener('click', closeModal);

    // Close on backdrop click
    modal.addEventListener('click', function (e) {
        if (e.target === modal) closeModal();
    });

    // Close on ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('active')) closeModal();
    });

    // Intersection Observer for scroll animations inside modal
    const items = modal.querySelectorAll('.timeline-item');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, {
        root: modal,
        threshold: 0.2
    });

    items.forEach(item => observer.observe(item));

    function checkTimelineScroll() {
        items.forEach(item => {
            const rect = item.getBoundingClientRect();
            if (rect.top < window.innerHeight * 0.9) {
                item.classList.add('visible');
            }
        });
    }
}

// ============================================
// PRODUCT QUICK VIEW MODAL
// ============================================
let currentQvProductId = null;

function initQuickView() {
    const modal = document.getElementById('productQuickViewModal');
    if (!modal) return;

    // Attach click listener to all premium product cards
    const cards = document.querySelectorAll('.product-card-premium, .vegam-product-card');
    cards.forEach(card => {
        card.style.cursor = 'pointer';
        
        const triggerQuickView = function (e) {
            // Prevent opening if clicked on 'Add to Cart' inside card
            if (e.target.closest('.card-add-btn, .vegam-btn')) return;

            const productId = this.getAttribute('data-product-id');
            
            // On products/category pages, open the full product page
            if (window.location.pathname.includes('/products')) {
                window.location.href = '/product/' + productId;
            } else {
                // On home page, use the Quick View pop
                openQuickView(this);
            }
        };

        card.addEventListener('click', triggerQuickView);
        card.addEventListener('touchend', function(e) {
            // Only trigger if it wasn't a swipe
            if (!window.isSwiping) {
                triggerQuickView.call(this, e);
            }
        }, { passive: true });
    });

    // Handle Tab Switching
    const tabHeaders = modal.querySelectorAll('.qv-tab-btn');
    const tabPanes = modal.querySelectorAll('.qv-pane');

    tabHeaders.forEach(btn => {
        btn.addEventListener('click', () => {
            // Remove active from all
            tabHeaders.forEach(h => h.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            // Add active to clicked
            btn.classList.add('active');
            const tabId = btn.getAttribute('data-tab');
            const targetPane = document.getElementById('tab-' + tabId);
            if (targetPane) targetPane.classList.add('active');
        });
    });
}

let qvSwiper = null;

function openQuickView(cardElement) {
    const modal = document.getElementById('productQuickViewModal');
    if (!modal) return;

    // Extract data from card
    currentQvProductId = cardElement.getAttribute('data-product-id');
    const name = cardElement.getAttribute('data-name');
    const subtitle = cardElement.getAttribute('data-subtitle') || 'Truly Bare Formulation';
    const desc = cardElement.getAttribute('data-desc') || 'A gentle botanical blend for everyday vitality.';
    const priceStr = cardElement.getAttribute('data-price') || '0.00';
    const mainImg = cardElement.getAttribute('data-img');
    const sub1 = cardElement.getAttribute('data-sub-img-1');
    const sub2 = cardElement.getAttribute('data-sub-img-2');
    const sub3 = cardElement.getAttribute('data-sub-img-3');
    const sub4 = cardElement.getAttribute('data-sub-img-4');
    
    const ingredients = cardElement.getAttribute('data-ingredients') || 'Formulated using the highest quality natural components...';
    const benefits = cardElement.getAttribute('data-benefits') || 'The rich botanicals work to actively restore balance and radiance...';
    const usage = cardElement.getAttribute('data-usage') || 'Use daily. Apply evenly to the desired area and let it absorb.';
    const warnings = cardElement.getAttribute('data-warnings') || 'Warnings: Consult a physician if pregnant or on medication. | Storage: Store in a cool, dry place.';

    // Populate modal text fields
    document.getElementById('qvTitle').textContent = name;
    document.getElementById('qvSubtitle').textContent = subtitle;
    document.getElementById('qvDesc').textContent = desc;

    // Format price
    const priceVal = parseFloat(priceStr);
    const originalPriceStr = cardElement.getAttribute('data-original-price');
    const discountPriceStr = cardElement.getAttribute('data-discount-price');
    
    const priceEl = document.getElementById('qvPrice');
    const originalEl = document.getElementById('qvOriginalPrice');
    
    if (discountPriceStr && discountPriceStr !== 'null' && discountPriceStr !== '') {
        const origVal = parseFloat(originalPriceStr);
        originalEl.textContent = '₹' + origVal.toLocaleString('en-IN', { minimumFractionDigits: 2 });
        originalEl.style.display = 'block';
        priceEl.textContent = '₹' + priceVal.toLocaleString('en-IN', { minimumFractionDigits: 2 });
        priceEl.classList.add('discounted');
    } else {
        originalEl.style.display = 'none';
        priceEl.textContent = '₹' + priceVal.toLocaleString('en-IN', { minimumFractionDigits: 2 });
        priceEl.classList.remove('discounted');
    }
    
    // Handle Out of Stock Thresholds
    const stock = parseInt(cardElement.getAttribute('data-stock') || '0');
    const qvAddBtn = document.querySelector('.qv-add-btn');
    const qvStockEl = document.getElementById('qvStockStatus');
    const forceSoldOut = cardElement.getAttribute('data-force-sold-out') === 'true';
    
    if (forceSoldOut || stock <= 3) {
        qvAddBtn.disabled = true;
        qvAddBtn.innerHTML = 'SOLD OUT';
        qvAddBtn.style.background = '#95a5a6';
        qvAddBtn.style.cursor = 'not-allowed';
        
        qvStockEl.innerHTML = '<span style="color: #e74c3c; font-weight: 700; letter-spacing: 1px;">SOLD OUT</span>';
        qvStockEl.style.display = 'block';
    } else if (stock < 10) {
        qvAddBtn.disabled = false;
        qvAddBtn.innerHTML = 'Add to Cart &rarr;';
        qvAddBtn.style.background = '';
        qvAddBtn.style.cursor = 'pointer';
        
        qvStockEl.innerHTML = '<span style="color: #f39c12;"><i class="bi bi-exclamation-triangle"></i> Limited Stock Available</span>';
        qvStockEl.style.display = 'block';
    } else {
        qvAddBtn.disabled = false;
        qvAddBtn.innerHTML = 'Add to Cart &rarr;';
        qvAddBtn.style.background = '';
        qvAddBtn.style.cursor = 'pointer';
        
        qvStockEl.style.display = 'none';
    }

    // Build Gallery Slides
    const wrapper = document.getElementById('qvSwiperWrapper');
    wrapper.innerHTML = ''; // Clear old slides

    const allImages = [mainImg, sub1, sub2, sub3, sub4].filter(src => src && src.trim() !== '' && src !== 'null');
    
    allImages.forEach((src, idx) => {
        const slide = document.createElement('div');
        slide.className = 'swiper-slide';
        slide.innerHTML = `<img src="${src}" alt="${name} - Image ${idx + 1}" ${idx === 0 ? 'id="qvImage"' : ''}>`;
        wrapper.appendChild(slide);
    });

    // Show modal first so Swiper can calculate dimensions
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';

    // Tabs Content
    document.getElementById('qvDetailsText').textContent = desc;
    document.getElementById('qvIngredientsText').textContent = ingredients;
    document.getElementById('qvBenefitsText').textContent = benefits;
    document.getElementById('qvUsageText').textContent = usage;
    document.getElementById('qvWarningsText').textContent = warnings;

    // Render Icons
    const category = cardElement.getAttribute('data-category');
    const skinType = cardElement.getAttribute('data-skin-type') || '';
    const hairType = cardElement.getAttribute('data-hair-type') || '';
    const iconsContainer = document.getElementById('qvTypeIcons');
    
    if (iconsContainer) {
        iconsContainer.innerHTML = '';
        const categoryUpper = (cardElement.getAttribute('data-category') || '').toUpperCase();
        const parentUpper = (cardElement.getAttribute('data-parent-category') || '').toUpperCase();
        
        const isSkinCare = categoryUpper.includes('SKIN') || categoryUpper.includes('SKN') || 
                           parentUpper.includes('SKIN') || parentUpper.includes('SKN');
        
        const isHairCare = categoryUpper.includes('HAIR') || parentUpper.includes('HAIR');

        if (isSkinCare) {
            const types = [
                { id: 'Oily', icon: 'bi-brightness-high' },
                { id: 'Dry', icon: 'bi-droplet' },
                { id: 'Sensitive', icon: 'bi-feather' },
                { id: 'Combo', icon: 'bi-circle-half' }
            ];
            types.forEach(t => {
                const isFit = skinType.includes(t.id);
                const html = `
                    <div class="icon-wrap ${!isFit ? 'unfit' : ''}">
                        <div class="icon-circle"><i class="bi ${t.icon}"></i></div>
                        <span class="icon-label">${t.id}</span>
                    </div>`;
                iconsContainer.insertAdjacentHTML('beforeend', html);
            });
            iconsContainer.style.display = 'flex';
        } else if (isHairCare) {
            const types = [
                { id: 'Straight', icon: 'bi-dash-lg' },
                { id: 'Wavy', icon: 'bi-activity' },
                { id: 'Curly', icon: 'bi-bezier2' },
                { id: 'Coily', icon: 'bi-hurricane' }
            ];
            types.forEach(t => {
                const isFit = hairType.includes(t.id);
                const html = `
                    <div class="icon-wrap ${!isFit ? 'unfit' : ''}">
                        <div class="icon-circle"><i class="bi ${t.icon}"></i></div>
                        <span class="icon-label">${t.id}</span>
                    </div>`;
                iconsContainer.insertAdjacentHTML('beforeend', html);
            });
            iconsContainer.style.display = 'flex';
        } else {
            iconsContainer.style.display = 'none';
        }
    }

    // Initialize or Update Swiper
    setTimeout(() => {
        if (qvSwiper) {
            qvSwiper.destroy(true, true);
        }
        qvSwiper = new Swiper('.qv-swiper', {
            loop: allImages.length > 1,
            pagination: { el: '.swiper-pagination', clickable: true },
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
            speed: 600,
            observer: true,
            observeParents: true
        });
    }, 100);
}

function closeQuickView() {
    const modal = document.getElementById('productQuickViewModal');
    if (!modal) return;
    modal.classList.remove('active');
    document.body.style.overflow = '';
    document.documentElement.style.overflow = '';
}

function addToCartFromQuickView() {
    if (!currentQvProductId) return;

    const qtyInput = document.getElementById('qvQuantity');
    const qty = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
    const priceEl = document.getElementById('qvPrice');
    const nameEl  = document.getElementById('qvTitle');
    const imgEl   = document.getElementById('qvImage');

    // Parse price from display string like "₹3,800.00" → 3800
    const priceStr = priceEl ? priceEl.textContent.replace(/[^\d.]/g, '') : '0';
    const price    = parseFloat(priceStr) || 0;
    const name     = nameEl ? nameEl.textContent.trim() : 'Premium formulation';
    const img      = imgEl ? imgEl.src : '';

    // Save to localStorage (same strategy as addToCart on home/products pages)
    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem('trulyBareCart') || '[]');
    } catch (e) { cart = []; }

    const existing = cart.find(i => String(i.id) === String(currentQvProductId));
    if (existing) {
        existing.qty += qty;
    } else {
        cart.push({ id: currentQvProductId, name: name, price: price, qty: qty, image: img });
    }
    localStorage.setItem('trulyBareCart', JSON.stringify(cart));

    // Update badge
    if (typeof updateGlobalCartBadge === 'function') updateGlobalCartBadge();

    // Visual feedback
    const btn = document.querySelector('.qv-add-btn');
    const originalHtml = btn ? btn.innerHTML : '';
    if (btn) {
        btn.innerHTML = '<i class="bi bi-check-lg"></i> Added';
        btn.style.opacity = '0.8';
        btn.disabled = true;
    }
    showToast(`✓ ${name} added to cart`);

    // Close modal and stay on page; reset button
    setTimeout(() => {
        closeQuickView();
        if (btn) {
            btn.innerHTML = originalHtml;
            btn.style.opacity = '';
            btn.disabled = false;
        }
    }, 1200);
}

// ============================================
// ANTI-GRAVITY CAROUSEL (2:1:2)
// ============================================
let agCurrentIndex = 0; // Logical index in infinite rotation
let totalAgCards = 0;
let agAutoPlayTimer = null;

function initAntiGravityCarousel() {
    const track = document.getElementById('agCarouselTrack');
    const prevBtn = document.getElementById('agPrevBtn');
    const nextBtn = document.getElementById('agNextBtn');
    const container = document.getElementById('agCarouselContainer');
    const cards = document.querySelectorAll('.ag-card');

    if (!track || cards.length === 0) return;

    totalAgCards = cards.length;

    // Initial render — card[0] starts at center
    updateAgCarousel();

    // Click on a card → bring it to center OR open quick view if already center
    cards.forEach((card, index) => {
        const handleAgClick = (e) => {
            const posClass = [...card.classList].find(c => c.startsWith('pos-'));
            if (!posClass) return;
            const delta = parseInt(posClass.replace('pos-', ''), 10);
            
            if (delta === 0) {
                // Already center → Navigate if data-link exists, else Open Quick View
                const link = card.getAttribute('data-link');
                if (link && link.trim()) {
                    if (link.startsWith('http')) {
                        window.open(link, '_blank', 'noopener,noreferrer');
                    } else {
                        window.location.href = link;
                    }
                } else {
                    openQuickView(card);
                }
            } else {
                // Not center → Rotate to center
                agCurrentIndex = (agCurrentIndex + delta + totalAgCards * 10) % totalAgCards;
                updateAgCarousel();
                resetAgAutoPlay();
            }
        };

        card.addEventListener('click', handleAgClick);
        card.style.cursor = 'pointer';
    });

    // Arrow buttons
    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            agCurrentIndex = (agCurrentIndex - 1 + totalAgCards) % totalAgCards;
            updateAgCarousel();
            resetAgAutoPlay();
        });
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            agCurrentIndex = (agCurrentIndex + 1) % totalAgCards;
            updateAgCarousel();
            resetAgAutoPlay();
        });
    }

    // Desktop hover → pause / resume
    if (container) {
        container.addEventListener('mouseenter', stopAgAutoPlay);
        container.addEventListener('mouseleave', startAgAutoPlay);
    }

    // Mobile touch → pause during interaction, resume after 1.5s
    if (container) {
        container.addEventListener('touchstart', () => {
            stopAgAutoPlay();
        }, { passive: true });

        container.addEventListener('touchend', () => {
            setTimeout(startAgAutoPlay, 1500);
        }, { passive: true });
    }

    startAgAutoPlay();
}

function updateAgCarousel() {
    const cards = document.querySelectorAll('.ag-card');
    cards.forEach((card, index) => {
        let relativePos = index - agCurrentIndex;

        // Wrap around to keep within –2 to +2 window
        if (relativePos < -2) relativePos += totalAgCards;
        if (relativePos > 2) relativePos -= totalAgCards;

        card.className = 'ag-card';
        card.classList.add(`pos-${relativePos}`);
    });
}

function startAgAutoPlay() {
    stopAgAutoPlay();
    agAutoPlayTimer = setInterval(() => {
        agCurrentIndex = (agCurrentIndex + 1) % totalAgCards;
        updateAgCarousel();
    }, 1500); // 1.5 seconds as requested
}

function stopAgAutoPlay() {
    if (agAutoPlayTimer) {
        clearInterval(agAutoPlayTimer);
        agAutoPlayTimer = null;
    }
}

function resetAgAutoPlay() {
    stopAgAutoPlay();
    startAgAutoPlay();
}