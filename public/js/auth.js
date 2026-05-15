document.addEventListener('DOMContentLoaded', () => {
  const cartMart = window.CartMart || {};
  const { apiFetch, buildReturnUrl, saveSession, getUser, getToken, clearSession } = cartMart;
  const authForm = document.getElementById('authForm');
  const authTitle = document.getElementById('authTitle');
  const authButton = document.getElementById('authButton');
  const authMode = document.body.getAttribute('data-auth-mode');

  if (!authForm || !apiFetch || !buildReturnUrl || !saveSession || !getUser || !getToken || !clearSession) {
    return;
  }

  if (authTitle) {
    authTitle.textContent = authMode === 'register' ? 'Create your account' : 'Welcome back';
  }

  if (authButton) {
    authButton.textContent = authMode === 'register' ? 'Create account' : 'Login';
  }

  (async () => {
    if (!getUser()) {
      return;
    }

    try {
      await apiFetch('/api/auth/me');
      window.location.href = '/';
    } catch (error) {
      clearSession();
    }
  })();

  authForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData(authForm);
    const payload = Object.fromEntries(formData.entries());
    const endpoint = authMode === 'register' ? '/api/auth/register' : '/api/auth/login';

    try {
      const response = await apiFetch(endpoint, {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      saveSession(response);
      window.location.href = buildReturnUrl();
    } catch (error) {
      const alert = document.getElementById('authAlert');
      if (alert) {
        alert.textContent = error.message;
        alert.classList.remove('d-none');
      }
    }
  });
});
