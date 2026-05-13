/* ============================================ */
/* TRULY BARE – REGISTER JS                      */
/* Password strength | UX micro-interactions    */
/* ============================================ */
'use strict';

document.addEventListener('DOMContentLoaded', function () {
    initPasswordStrength();
    initPasswordToggle();
    initPasswordMatch();
    initFormSubmit();
});

/* ===== PASSWORD STRENGTH ===== */
function initPasswordStrength() {
    const pwInput = document.getElementById('password');
    const bar     = document.querySelector('.strength-bar');
    if (!pwInput || !bar) return;

    pwInput.addEventListener('input', function () {
        const score = calcStrength(this.value);
        const widths = ['0%', '25%', '50%', '75%', '100%'];
        const colors = ['transparent', '#E8A0A0', '#C9A84C', '#8BAF70', '#4CAF50'];
        bar.style.width    = widths[score];
        bar.style.background = colors[score];
    });
}

function calcStrength(pw) {
    if (!pw) return 0;
    let score = 0;
    if (pw.length >= 8)         score++;
    if (/[A-Z]/.test(pw))       score++;
    if (/[0-9]/.test(pw))       score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    return score;
}

/* ===== PASSWORD TOGGLE ===== */
function initPasswordToggle() {
    document.querySelectorAll('.toggle-password').forEach(btn => {
        btn.addEventListener('click', function () {
            const input = document.getElementById(this.dataset.target);
            if (!input) return;
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            const icon = this.querySelector('i');
            if (icon) icon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';
        });
    });
}

/* ===== PASSWORD MATCH ===== */
function initPasswordMatch() {
    const pw      = document.getElementById('password');
    const confirm = document.getElementById('confirmPassword');
    const hint    = document.getElementById('matchHint');
    if (!pw || !confirm) return;

    function check() {
        if (!confirm.value) { if (hint) hint.textContent = ''; return; }
        const match = pw.value === confirm.value;
        confirm.style.borderColor = match ? 'rgba(76,175,80,0.5)' : 'rgba(139,26,26,0.4)';
        if (hint) {
            hint.textContent = match ? '✓ Passwords match' : 'Passwords do not match';
            hint.style.color  = match ? '#4CAF50' : '#8B1A1A';
        }
    }

    pw.addEventListener('input', check);
    confirm.addEventListener('input', check);
}

/* ===== FORM SUBMIT ===== */
function initFormSubmit() {
    const form = document.querySelector('form');
    if (!form) return;

    form.addEventListener('submit', function () {
        const btn = this.querySelector('.btn-submit');
        if (!btn) return;
        btn.innerHTML = '<i class="bi bi-hourglass-split spin"></i> Creating Account…';
        btn.disabled  = true;
        setTimeout(() => {
            btn.disabled  = false;
            btn.innerHTML = '<i class="bi bi-person-plus"></i> Create Account';
        }, 6000);
    });

    const style = document.createElement('style');
    style.textContent = '@keyframes spin { to { transform: rotate(360deg); } } .spin { display:inline-block; animation: spin 0.8s linear infinite; }';
    document.head.appendChild(style);
}

window.addEventListener('scroll', () => {
    const header = document.querySelector('.header');
    if (header) header.classList.toggle('scrolled', window.scrollY > 20);
}, { passive: true });

console.log('✨ Truly Bare | Register Module Loaded');
