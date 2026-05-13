/* ============================================ */
/* TRULY BARE – CART JS                          */
/* Quantity | Remove | Totals | Luxury UX       */
/* ============================================ */
'use strict';

document.addEventListener('DOMContentLoaded', function () {
    loadCartFromLocalStorage();   // ← must run first to inject items
    initQuantitySteppers();
    initRemoveButtons();
    initCoupon();
    animateCartItems();
    updateCartBadge();
    initSyncToCheckout();
    console.log('✨ Truly Bare | Cart Module Loaded');
});

function initSyncToCheckout() {
    const btn = document.querySelector('.btn-checkout');
    if (!btn) return;

    btn.addEventListener('click', function(e) {
        const cart = JSON.parse(localStorage.getItem('trulyBareCart') || '[]');
        if (cart.length === 0) {
            e.preventDefault();
            alert('Your cart is empty');
            return;
        }

        // Only sync if we are going to /checkout
        if (this.getAttribute('href') === '/checkout' || this.getAttribute('href').includes('/checkout')) {
            e.preventDefault();
            
            const syncData = cart.map(item => ({
                productId: item.id,
                quantity: item.qty
            }));

            btn.innerHTML = '<i class="bi bi-hourglass-split"></i> Preparing Checkout...';
            btn.style.pointerEvents = 'none';

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            
            const fetchHeaders = {
                'Content-Type': 'application/json'
            };
            if (csrfToken && csrfHeader) {
                fetchHeaders[csrfHeader] = csrfToken;
            }

            fetch('/cart/sync', {
                method: 'POST',
                headers: fetchHeaders,
                body: JSON.stringify(syncData)
            })
            .then(response => {
                if (response.ok || response.redirected) {
                    window.location.href = '/checkout';
                } else {
                    console.error('Sync failed');
                    window.location.href = '/checkout'; // Fallback
                }
            })
            .catch(err => {
                console.error('Sync error:', err);
                window.location.href = '/checkout'; // Fallback
            });
        }
    });
}



/* ===== LOCALSTORAGE CART LOADER ===== */
function loadCartFromLocalStorage() {
    console.log('🛒 Truly Bare | Loading cart from storage...');
    const layout    = document.getElementById('cartLayout');
    const emptyDiv  = document.getElementById('cartEmpty');
    const container = document.getElementById('cartItemsContainer');
    if (!layout || !emptyDiv || !container) {
        console.warn('⚠️ Cart UI elements missing from DOM');
        return;
    }

    // Read cart from localStorage
    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem('trulyBareCart') || '[]');
    } catch(e) {
        console.error('❌ Failed to parse cart from localStorage', e);
        cart = [];
    }

    // Cleanup corrupted entries
    cart = cart.filter(function(i) { return i && i.name && i.qty > 0; });

    // Check if the server provided items (e.g. from backend session)
    const serverItems = container.querySelectorAll('.cart-item');

    if (cart.length === 0) {
        if (serverItems.length > 0) {
            console.log('ℹ️ Local cart empty, but server items found');
            layout.style.display   = 'grid'; // .cart-layout is display:grid
            emptyDiv.style.display = 'none';
            updateCartTotal();
        } else {
            console.log('ℹ️ Cart is truly empty');
            layout.style.display   = 'none';
            emptyDiv.style.display = 'block';
        }
        return;
    }

    // Build DOM items from localStorage
    console.log(`📦 Injecting ${cart.length} items from localStorage`);
    container.innerHTML = '';

    cart.forEach(function(item, idx) {
        var div = document.createElement('div');
        div.className = 'cart-item animate-fade-up';
        div.dataset.price = item.price;
        div.dataset.id    = item.id || idx;
        
        const fallbackImg = 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=200&h=240&fit=crop';
        const displayImg  = item.image && item.image.startsWith('http') ? item.image : fallbackImg;

        div.innerHTML = [
            '<div class="cart-item-img">',
            '  <img src="' + displayImg + '" alt="' + escHtml(item.name) + '" onerror="this.src=\''+fallbackImg+'\'">',
            '</div>',
            '<div class="cart-item-details">',
            '  <span class="cart-item-number">ITEM ' + String(idx + 1).padStart(2, '0') + '</span>',
            '  <h3 class="cart-item-name">' + escHtml(item.name) + '</h3>',
            '  <p class="cart-item-desc">Luxury botanical formulation</p>',
            '  <div class="cart-item-footer">',
            '    <div class="cart-item-price">\u20b9' + (item.price * item.qty).toLocaleString('en-IN') + '</div>',
            '    <div class="qty-stepper">',
            '      <button class="qty-btn qty-minus" type="button" aria-label="Decrease">\u2212</button>',
            '      <span class="qty-display">' + item.qty + '</span>',
            '      <button class="qty-btn qty-plus" type="button" aria-label="Increase">+</button>',
            '    </div>',
            '    <button class="remove-btn" type="button"><i class="bi bi-x"></i> Remove</button>',
            '  </div>',
            '</div>'
        ].join('');
        container.appendChild(div);
    });

    // Show cart, hide empty
    layout.style.display   = 'grid';
    emptyDiv.style.display = 'none';

    // Final calculations
    updateCartTotal();
    animateCartItems();
}

function escHtml(str) {
    return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

/* ===== SYNC BADGE ===== */
function updateCartBadge() {
    if (typeof updateGlobalCartBadge === 'function') {
        updateGlobalCartBadge();
    }
}

/* ===== SAVE CART BACK TO LOCALSTORAGE (called on qty change / remove) ===== */
function syncCartToLocalStorage() {
    var cart = [];
    document.querySelectorAll('.cart-item').forEach(function(item) {
        var name  = (item.querySelector('.cart-item-name') || {}).textContent || 'Product';
        var price = parseFloat(item.dataset.price || 0);
        var qty   = parseInt((item.querySelector('.qty-display') || {}).textContent || 1);
        var id    = parseInt(item.dataset.id || 0);
        if (qty > 0) cart.push({ id: id, name: name, price: price, qty: qty });
    });
    localStorage.setItem('trulyBareCart', JSON.stringify(cart));
    updateCartBadge();
}


/* ===== QUANTITY STEPPERS ===== */
function initQuantitySteppers() {
    document.querySelectorAll('.qty-stepper').forEach(stepper => {
        const minusBtn  = stepper.querySelector('.qty-minus');
        const plusBtn   = stepper.querySelector('.qty-plus');
        const display   = stepper.querySelector('.qty-display');
        const itemCard  = stepper.closest('.cart-item');

        if (!minusBtn || !plusBtn || !display) return;

        minusBtn.addEventListener('click', () => {
            let val = parseInt(display.textContent) || 1;
            if (val <= 1) {
                // Shake animation instead of going below 1
                itemCard?.classList.add('shake');
                setTimeout(() => itemCard?.classList.remove('shake'), 400);
                return;
            }
            val--;
            display.textContent = val;
            updateItemPrice(itemCard, val);
            updateCartTotal();
            syncCartToLocalStorage();
        });

        plusBtn.addEventListener('click', () => {
            let val = parseInt(display.textContent) || 1;
            if (val >= 10) return;
            val++;
            display.textContent = val;
            updateItemPrice(itemCard, val);
            updateCartTotal();
            syncCartToLocalStorage();
        });
    });

    // Add shake animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes shake { 0%,100%{transform:translateX(0)} 25%{transform:translateX(-4px)} 75%{transform:translateX(4px)} }
        .shake { animation: shake 0.3s ease; }
        @keyframes priceFlash { 0%{color:var(--sapphire-gold)} 100%{color:var(--bold-red)} }
        .price-flash { animation: priceFlash 0.4s ease; }
    `;
    document.head.appendChild(style);
}

function updateItemPrice(itemCard, qty) {
    if (!itemCard) return;
    const basePrice = parseFloat(itemCard.dataset.price || 0);
    if (!basePrice) return;
    const priceEl = itemCard.querySelector('.cart-item-price');
    if (priceEl) {
        priceEl.textContent = '₹' + (basePrice * qty).toLocaleString('en-IN');
        priceEl.classList.add('price-flash');
        setTimeout(() => priceEl.classList.remove('price-flash'), 400);
    }
}

function updateCartTotal() {
    let total = 0;
    document.querySelectorAll('.cart-item').forEach(item => {
        const basePrice = parseFloat(item.dataset.price || 0);
        const qty       = parseInt(item.querySelector('.qty-display')?.textContent || 1);
        total += basePrice * qty;
    });

    const subtotalEl = document.getElementById('cart-subtotal');
    const totalEl    = document.getElementById('cart-total');
    if (subtotalEl) subtotalEl.textContent = '₹' + total.toLocaleString('en-IN');
    if (totalEl)    totalEl.textContent    = '₹' + total.toLocaleString('en-IN');
}

/* ===== REMOVE ITEM ===== */
function initRemoveButtons() {
    document.querySelectorAll('.remove-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const item = this.closest('.cart-item');
            if (!item) return;

            item.style.transition = 'opacity 0.35s ease, transform 0.35s ease, max-height 0.45s ease';
            item.style.opacity = '0';
            item.style.transform = 'translateX(30px)';
            item.style.maxHeight = item.offsetHeight + 'px';

            setTimeout(() => {
                item.style.maxHeight = '0';
                item.style.padding   = '0';
                item.style.margin    = '0';
                item.style.overflow  = 'hidden';
            }, 300);

            setTimeout(() => {
                item.remove();
                updateCartTotal();
                syncCartToLocalStorage();
                checkEmptyCart();
            }, 750);
        });
    });
}

function checkEmptyCart() {
    const items    = document.querySelectorAll('.cart-item');
    const layout   = document.getElementById('cartLayout');
    const emptyDiv = document.getElementById('cartEmpty');

    if (items.length === 0) {
        if (layout)   layout.style.display   = 'none';
        if (emptyDiv) emptyDiv.style.display  = '';
        // Clear localStorage too
        localStorage.removeItem('trulyBareCart');
        updateCartBadge();
    }
}

/* ===== COUPON ===== */
function initCoupon() {
    const applyBtn = document.querySelector('.coupon-apply');
    const input    = document.querySelector('.coupon-input');
    if (!applyBtn || !input) return;

    const validCoupons = { 'LUXURY10': 10, 'BARE20': 20, 'GOLD15': 15 };

    applyBtn.addEventListener('click', () => {
        const code = input.value.trim().toUpperCase();
        if (validCoupons[code]) {
            const discount = validCoupons[code];
            showCouponMessage(`Coupon applied — ${discount}% off`, 'success');
            applyCouponDiscount(discount);
            input.disabled   = true;
            applyBtn.disabled = true;
            applyBtn.textContent = 'Applied';
        } else {
            showCouponMessage('Invalid coupon code', 'error');
            input.style.borderColor = 'rgba(139,26,26,0.5)';
            setTimeout(() => input.style.borderColor = '', 2000);
        }
    });
}

function showCouponMessage(msg, type) {
    let el = document.getElementById('coupon-msg');
    if (!el) {
        el = document.createElement('div');
        el.id = 'coupon-msg';
        el.style.cssText = 'margin-top:8px;font-size:12px;letter-spacing:0.5px;';
        document.querySelector('.coupon-section')?.appendChild(el);
    }
    el.textContent  = msg;
    el.style.color  = type === 'success' ? '#4CAF50' : '#8B1A1A';
}

function applyCouponDiscount(pct) {
    const subtotalEl  = document.getElementById('cart-subtotal');
    const discountEl  = document.getElementById('cart-discount');
    const totalEl     = document.getElementById('cart-total');
    if (!subtotalEl || !totalEl) return;

    const subtotal  = parseInt(subtotalEl.textContent.replace(/[^\d]/g, ''));
    const discount  = Math.round(subtotal * pct / 100);
    const newTotal  = subtotal - discount;

    if (discountEl) {
        discountEl.textContent = '-₹' + discount.toLocaleString('en-IN');
        discountEl.style.color = '#4CAF50';
    }
    totalEl.textContent = '₹' + newTotal.toLocaleString('en-IN');
}




/* ===== ENTRANCE ANIMATIONS ===== */
function animateCartItems() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry, i) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity    = '1';
                    entry.target.style.transform  = 'translateY(0)';
                }, i * 80);
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.cart-item').forEach(item => {
        item.style.opacity   = '0';
        item.style.transform = 'translateY(20px)';
        item.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        observer.observe(item);
    });
}

window.TrulyBareCart = { updateCartTotal, checkEmptyCart };
