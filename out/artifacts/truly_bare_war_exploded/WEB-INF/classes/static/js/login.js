/* ============================================ */
/* TRULY BARE – LOGIN JS                         */
/* Micro-interactions | Luxury UX               */
/* ============================================ */
'use strict';

document.addEventListener('DOMContentLoaded', function () {
    initInputEffects();
    initFormSubmit();
    initPasswordToggle();
});

/* ===== FLOATING LABEL EFFECT ===== */
function initInputEffects() {
    const inputs = document.querySelectorAll('.form-input');
    inputs.forEach(input => {
        input.addEventListener('focus', () => {
            input.closest('.form-group')?.classList.add('focused');
        });
        input.addEventListener('blur', () => {
            input.closest('.form-group')?.classList.remove('focused');
        });
    });
}

/* ===== PASSWORD TOGGLE ===== */
function initPasswordToggle() {
    const toggleBtns = document.querySelectorAll('.toggle-password');
    toggleBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            const inputId = this.dataset.target;
            const input   = document.getElementById(inputId);
            if (!input) return;

            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';

            const icon = this.querySelector('i');
            if (icon) {
                icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
            }
        });
    });
}

/* ===== FORM SUBMIT LOADING STATE ===== */
function initFormSubmit() {
    const form = document.querySelector('form');
    if (!form) return;

    form.addEventListener('submit', function () {
        const btn = this.querySelector('.btn-submit');
        if (!btn) return;
        btn.innerHTML = '<i class="bi bi-hourglass-split spin"></i> Signing in…';
        btn.disabled = true;

        // Re-enable after 5s as fallback
        setTimeout(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-box-arrow-in-right"></i> Sign In';
        }, 5000);
    });

    // Add spin keyframes
    const style = document.createElement('style');
    style.textContent = '@keyframes spin { to { transform: rotate(360deg); } } .spin { display:inline-block; animation: spin 0.8s linear infinite; }';
    document.head.appendChild(style);
}

/* ===== HEADER SCROLL ===== */
window.addEventListener('scroll', () => {
    const header = document.querySelector('.header');
    if (header) header.classList.toggle('scrolled', window.scrollY > 20);
}, { passive: true });

console.log('✨ Truly Bare | Login Module Loaded');
