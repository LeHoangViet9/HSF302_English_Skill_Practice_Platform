// static/js/auth/auth.js — Vanilla JS dùng chung cho các trang auth (login, ...)

// 1. Toggle hiện/ẩn mật khẩu
document.querySelectorAll('.password-toggle').forEach((btn) => {
    btn.addEventListener('click', () => {
        const input = document.getElementById(btn.dataset.target);
        if (!input) return;
        const isHidden = input.type === 'password';
        input.type = isHidden ? 'text' : 'password';
        btn.classList.toggle('is-visible', isHidden);
        btn.setAttribute('aria-label', isHidden ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
    });
});

// 2. Disable nút submit + đổi text khi đăng nhập đang xử lý
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', () => {
        const submitBtn = loginForm.querySelector('.btn-login');
        if (!submitBtn) return;
        loginForm.setAttribute('aria-busy', 'true');
        submitBtn.classList.add('is-loading');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang đăng nhập...';
    });
}

// 3. Disable nút submit + đổi text khi form quên/đặt lại mật khẩu đang xử lý
const resetPasswordForm = document.getElementById('resetPasswordForm');
if (resetPasswordForm) {
    resetPasswordForm.addEventListener('submit', () => {
        const submitBtn = resetPasswordForm.querySelector('.btn-rp-submit');
        if (!submitBtn) return;
        resetPasswordForm.setAttribute('aria-busy', 'true');
        submitBtn.classList.add('is-loading');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang xử lý...';
    });
}

const forgotPasswordForm = document.getElementById('forgotPasswordForm');
if (forgotPasswordForm) {
    forgotPasswordForm.addEventListener('submit', () => {
        const submitBtn = forgotPasswordForm.querySelector('.btn-fp-submit');
        if (!submitBtn) return;
        forgotPasswordForm.setAttribute('aria-busy', 'true');
        submitBtn.classList.add('is-loading');
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang gửi...';
    });
}
