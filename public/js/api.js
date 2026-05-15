const TOKEN_KEY = 'cartmartbd_token';
const USER_KEY = 'cartmartbd_user';
const CART_KEY = 'cartmartbd_cart';

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

function saveSession(data) {
  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

function getCart() {
  const raw = localStorage.getItem(CART_KEY);
  return raw ? JSON.parse(raw) : [];
}

function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
}

function addToCart(product) {
  const cart = getCart();
  const existing = cart.find((item) => item.productId === product._id);

  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({
      productId: product._id,
      name: product.name,
      price: product.price,
      imageUrl: product.imageUrl,
      quantity: 1,
      stock: product.stock,
      category: product.category
    });
  }

  saveCart(cart);
  return cart;
}

function updateCartItem(productId, quantity) {
  const cart = getCart().map((item) => {
    if (item.productId === productId) {
      return { ...item, quantity };
    }
    return item;
  }).filter((item) => item.quantity > 0);

  saveCart(cart);
  return cart;
}

function removeFromCart(productId) {
  const cart = getCart().filter((item) => item.productId !== productId);
  saveCart(cart);
  return cart;
}

function clearCart() {
  saveCart([]);
}

function formatCurrency(value) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(Number(value || 0));
}

function cartCount() {
  return getCart().reduce((total, item) => total + item.quantity, 0);
}

async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, {
    ...options,
    headers
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : {};

  if (!response.ok) {
    if (response.status === 401 && token) {
      clearSession();
      if (window.location.pathname !== '/login.html' && window.location.pathname !== '/register.html') {
        window.location.href = `/login.html?returnUrl=${encodeURIComponent(window.location.pathname)}`;
      }
    }
    const error = new Error(data.message || 'Request failed');
    if (response.status === 401 && token) {
      error.code = 'AUTH_EXPIRED';
    }
    throw error;
  }

  return data;
}

async function validateSession() {
  const token = getToken();
  if (!token) {
    return null;
  }

  try {
    const response = await fetch('/api/auth/me', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (!response.ok) {
      clearSession();
      return null;
    }

    const data = await response.json();
    if (data.user) {
      localStorage.setItem(USER_KEY, JSON.stringify(data.user));
      return data.user;
    }

    clearSession();
    return null;
  } catch (error) {
    clearSession();
    return null;
  }
}

function setText(selector, value) {
  const node = document.querySelector(selector);
  if (node) {
    node.textContent = value;
  }
}

function updateNav() {
  const user = getUser();
  const guestLinks = document.querySelectorAll('[data-auth-state="guest"]');
  const userLinks = document.querySelectorAll('[data-auth-state="user"]');
  const adminLinks = document.querySelectorAll('[data-auth-state="admin"]');
  const userLabel = document.querySelectorAll('[data-user-label]');
  const cartBadges = document.querySelectorAll('[data-cart-count]');

  guestLinks.forEach((node) => {
    node.classList.toggle('d-none', !!user);
  });
  userLinks.forEach((node) => {
    node.classList.toggle('d-none', !user);
  });
  adminLinks.forEach((node) => {
    node.classList.toggle('d-none', !user || user.role !== 'admin');
  });
  userLabel.forEach((node) => {
    node.textContent = user ? `${user.name} (${user.role})` : '';
  });
  cartBadges.forEach((node) => {
    node.textContent = cartCount();
  });
}

function bindLogoutButtons() {
  document.querySelectorAll('[data-logout]').forEach((button) => {
    button.addEventListener('click', () => {
      clearSession();
      window.location.href = '/';
    });
  });
}

async function initShell() {
  await validateSession();
  updateNav();
  bindLogoutButtons();
}

function requireAuth(role) {
  const user = getUser();
  if (!user) {
    window.location.href = `/login.html?returnUrl=${encodeURIComponent(window.location.pathname)}`;
    return null;
  }

  if (role && user.role !== role) {
    window.location.href = '/';
    return null;
  }

  return user;
}

function buildReturnUrl() {
  const params = new URLSearchParams(window.location.search);
  return params.get('returnUrl') || '/';
}

window.CartMart = {
  apiFetch,
  addToCart,
  cartCount,
  clearCart,
  clearSession,
  formatCurrency,
  getCart,
  getToken,
  getUser,
  initShell,
  validateSession,
  requireAuth,
  removeFromCart,
  saveCart,
  saveSession,
  setText,
  updateCartItem,
  updateNav,
  buildReturnUrl
};

// Initialize shell on all pages
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initShell);
} else {
  initShell();
}
