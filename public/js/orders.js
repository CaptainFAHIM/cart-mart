document.addEventListener('DOMContentLoaded', () => {
  const cartMart = window.CartMart || {};
  const { apiFetch, formatCurrency, requireAuth } = cartMart;

  const ordersList = document.getElementById('ordersList');

  if (!ordersList || !apiFetch) {
    console.error('CartMartBD: Missing required elements or API');
    return;
  }

  requireAuth();

  function renderOrders(orders) {
    if (!orders.length) {
      ordersList.innerHTML = `
        <div class="empty-state glass-card">
          <h3 class="mb-2">No orders yet</h3>
          <p class="mb-0">Once you place your first order, it will appear here.</p>
        </div>
      `;
      return;
    }

    ordersList.innerHTML = orders.map((order) => `
      <article class="order-card p-3 p-lg-4 mb-3">
        <div class="d-flex flex-wrap justify-content-between gap-3 mb-3">
          <div>
            <h3 class="h5 mb-1">Order #${order._id.slice(-6).toUpperCase()}</h3>
            <div class="text-soft small">${new Date(order.createdAt).toLocaleString()}</div>
          </div>
          <div class="text-end">
            <div class="badge text-bg-success mb-2">${order.status}</div>
            <div class="fw-bold">${formatCurrency(order.totalPrice)}</div>
          </div>
        </div>
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead>
              <tr>
                <th>Product</th>
                <th>Qty</th>
                <th>Price</th>
                <th>Line Total</th>
              </tr>
            </thead>
            <tbody>
              ${order.items.map((item) => `
                <tr>
                  <td>${item.name}</td>
                  <td>${item.quantity}</td>
                  <td>${formatCurrency(item.price)}</td>
                  <td>${formatCurrency(item.price * item.quantity)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </article>
    `).join('');
  }

  async function loadOrders() {
    const response = await apiFetch('/api/orders/mine');
    renderOrders(response.orders || []);
  }

  loadOrders().catch((error) => {
    ordersList.innerHTML = `
      <div class="empty-state glass-card text-danger">
        <h3 class="mb-2">Unable to load orders</h3>
        <p class="mb-0">${error.message}</p>
      </div>
    `;
  });
});
