document.addEventListener('DOMContentLoaded', () => {
  const cartMart = window.CartMart || {};
  const {
    apiFetch,
    clearCart,
    formatCurrency,
    getCart,
    getUser,
    removeFromCart,
    updateCartItem,
    updateNav
  } = cartMart;

  const cartList = document.getElementById('cartList');
  const cartSummary = document.getElementById('cartSummary');
  const checkoutForm = document.getElementById('checkoutForm');

  if (!cartList || !cartSummary || !checkoutForm || !apiFetch) {
    console.error('CartMartBD: Missing required elements for cart');
    return;
  }

  function getTotals(cart) {
    const subtotal = cart.reduce((total, item) => total + item.price * item.quantity, 0);
    const shipping = subtotal > 0 ? 4.99 : 0;
    return {
      subtotal,
      shipping,
      total: subtotal + shipping
    };
  }

  function renderCart() {
    const cart = getCart();
    const totals = getTotals(cart);

    if (!cart.length) {
      cartList.innerHTML = `
        <div class="empty-state glass-card">
          <h3 class="mb-2">Your cart is empty</h3>
          <p class="mb-0">Browse the catalog and add some products to continue.</p>
        </div>
      `;
      cartSummary.innerHTML = '';
      return;
    }

    cartList.innerHTML = cart.map((item) => `
      <article class="order-card p-3 p-lg-4 mb-3">
        <div class="row g-3 align-items-center">
          <div class="col-md-2">
            <img class="img-fluid rounded-4" src="${item.imageUrl}" alt="${item.name}">
          </div>
          <div class="col-md-4">
            <h3 class="h5 mb-1">${item.name}</h3>
            <p class="text-soft small mb-0">${item.category || 'Cart item'}</p>
          </div>
          <div class="col-md-2">
            <strong>${formatCurrency(item.price)}</strong>
          </div>
          <div class="col-md-2">
            <div class="input-group">
              <button class="btn btn-outline-light" data-decrease="${item.productId}">-</button>
              <input class="form-control text-center" value="${item.quantity}" readonly>
              <button class="btn btn-outline-light" data-increase="${item.productId}">+</button>
            </div>
          </div>
          <div class="col-md-2 text-md-end">
            <div class="fw-bold mb-2">${formatCurrency(item.price * item.quantity)}</div>
            <button class="btn btn-sm btn-outline-light" data-remove="${item.productId}">Remove</button>
          </div>
        </div>
      </article>
    `).join('');

    cartSummary.innerHTML = `
      <div class="glass-card p-4">
        <h3 class="h5 mb-3">Order Summary</h3>
        <div class="d-flex justify-content-between mb-2"><span>Subtotal</span><strong>${formatCurrency(totals.subtotal)}</strong></div>
        <div class="d-flex justify-content-between mb-2"><span>Shipping</span><strong>${formatCurrency(totals.shipping)}</strong></div>
        <div class="d-flex justify-content-between fs-5"><span>Total</span><strong>${formatCurrency(totals.total)}</strong></div>
      </div>
    `;

    document.querySelectorAll('[data-increase]').forEach((button) => {
      button.addEventListener('click', () => {
        const item = cart.find((entry) => entry.productId === button.getAttribute('data-increase'));
        if (item) {
          updateCartItem(item.productId, item.quantity + 1);
          renderCart();
          updateNav();
        }
      });
    });

    document.querySelectorAll('[data-decrease]').forEach((button) => {
      button.addEventListener('click', () => {
        const item = cart.find((entry) => entry.productId === button.getAttribute('data-decrease'));
        if (item) {
          updateCartItem(item.productId, item.quantity - 1);
          renderCart();
          updateNav();
        }
      });
    });

    document.querySelectorAll('[data-remove]').forEach((button) => {
      button.addEventListener('click', () => {
        removeFromCart(button.getAttribute('data-remove'));
        renderCart();
        updateNav();
      });
    });
  }

  checkoutForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const cart = getCart();

    if (!cart.length) {
      return;
    }

    if (!getUser()) {
      window.location.href = `/login.html?returnUrl=${encodeURIComponent('/cart.html')}`;
      return;
    }

    const formData = new FormData(checkoutForm);
    const payload = {
      items: cart.map((item) => ({ productId: item.productId, quantity: item.quantity })),
      shippingName: formData.get('shippingName'),
      shippingPhone: formData.get('shippingPhone'),
      shippingAddress: formData.get('shippingAddress'),
      paymentMethod: formData.get('paymentMethod')
    };

    const alert = document.getElementById('checkoutAlert');

    try {
      const response = await apiFetch('/api/orders', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      clearCart();
      updateNav();
      renderCart();
      alert.classList.remove('d-none', 'alert-danger');
      alert.classList.add('alert-success');
      alert.textContent = `Order placed successfully. Order total: ${formatCurrency(response.order.totalPrice)}`;
      checkoutForm.reset();
    } catch (error) {
      if (error.code === 'AUTH_EXPIRED' || /invalid or expired token/i.test(error.message)) {
        alert.classList.add('d-none');
        return;
      }

      alert.classList.remove('d-none', 'alert-success');
      alert.classList.add('alert-danger');
      alert.textContent = error.message;
    }
  });

  renderCart();
});
