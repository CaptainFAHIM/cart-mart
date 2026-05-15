document.addEventListener('DOMContentLoaded', () => {
  const cartMart = window.CartMart || {};
  const { apiFetch, formatCurrency, requireAuth, updateNav } = cartMart;

  const adminUser = requireAuth('admin');
  const statsGrid = document.getElementById('adminStats');
  const productsTable = document.getElementById('productsTable');
  const ordersTable = document.getElementById('ordersTable');
  const productForm = document.getElementById('productForm');
  const productFormTitle = document.getElementById('productFormTitle');
  const productSubmit = document.getElementById('productSubmit');
  const productIdField = document.getElementById('productId');
  const resetProductButton = document.getElementById('resetProductButton');

  const statusOptions = ['Pending', 'Processing', 'Shipped', 'Delivered', 'Cancelled'];

  if (!adminUser) {
    throw new Error('Admin access is required');
  }

  function resetProductForm() {
    productForm.reset();
    productIdField.value = '';
    productFormTitle.textContent = 'Add product';
    productSubmit.textContent = 'Save product';
  }

  resetProductButton?.addEventListener('click', resetProductForm);

  function renderStats(stats) {
    statsGrid.innerHTML = [
      { label: 'Products', value: stats.productCount },
      { label: 'Orders', value: stats.orderCount },
      { label: 'Users', value: stats.userCount },
      { label: 'Pending', value: stats.pendingOrders }
    ].map((item) => `
      <div class="col-md-3">
        <div class="mini-stat">
          <span class="text-soft d-block">${item.label}</span>
          <strong>${item.value}</strong>
        </div>
      </div>
    `).join('');
  }

  function renderProducts(products) {
    if (!products.length) {
      productsTable.innerHTML = '<p class="text-soft mb-0">No products available.</p>';
      return;
    }

    productsTable.innerHTML = `
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${products.map((product) => `
              <tr>
                <td>${product.name}</td>
                <td>${product.category}</td>
                <td>${formatCurrency(product.price)}</td>
                <td>${product.stock}</td>
                <td>
                  <button class="btn btn-sm btn-outline-light me-2" data-edit-product="${encodeURIComponent(JSON.stringify(product))}">Edit</button>
                  <button class="btn btn-sm btn-outline-danger" data-delete-product="${product._id}">Delete</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;

    document.querySelectorAll('[data-edit-product]').forEach((button) => {
      button.addEventListener('click', () => {
        const product = JSON.parse(decodeURIComponent(button.getAttribute('data-edit-product')));
        productIdField.value = product._id;
        productFormTitle.textContent = `Edit ${product.name}`;
        productSubmit.textContent = 'Update product';
        productForm.elements.name.value = product.name;
        productForm.elements.description.value = product.description;
        productForm.elements.category.value = product.category;
        productForm.elements.imageUrl.value = product.imageUrl;
        productForm.elements.price.value = product.price;
        productForm.elements.stock.value = product.stock;
        productForm.elements.featured.checked = Boolean(product.featured);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
    });

    document.querySelectorAll('[data-delete-product]').forEach((button) => {
      button.addEventListener('click', async () => {
        if (!confirm('Delete this product?')) {
          return;
        }

        await apiFetch(`/api/products/${button.getAttribute('data-delete-product')}`, {
          method: 'DELETE'
        });

        await refreshAdminData();
      });
    });
  }

  function renderOrders(orders) {
    if (!orders.length) {
      ordersTable.innerHTML = '<p class="text-soft mb-0">No orders available.</p>';
      return;
    }

    ordersTable.innerHTML = `
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>Order</th>
              <th>Customer</th>
              <th>Items</th>
              <th>Total</th>
              <th>Status</th>
              <th>Update</th>
            </tr>
          </thead>
          <tbody>
            ${orders.map((order) => `
              <tr>
                <td>#${order._id.slice(-6).toUpperCase()}</td>
                <td>${order.user?.name || 'Unknown'}<br><span class="text-soft small">${order.user?.email || ''}</span></td>
                <td>
                  <div class="d-grid gap-2">
                    ${order.items.map((item) => `
                      <div class="small">
                        <div class="fw-semibold">${item.product?.name || item.name}</div>
                        <div class="text-soft">Qty: ${item.quantity} · ${formatCurrency(item.price)} each</div>
                      </div>
                    `).join('')}
                  </div>
                </td>
                <td>${formatCurrency(order.totalPrice)}</td>
                <td>
                  <select class="form-select form-select-sm" data-order-status="${order._id}">
                    ${statusOptions.map((status) => `<option value="${status}" ${status === order.status ? 'selected' : ''}>${status}</option>`).join('')}
                  </select>
                </td>
                <td>
                  <button class="btn btn-sm btn-accent" data-save-order="${order._id}">Save</button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;

    document.querySelectorAll('[data-save-order]').forEach((button) => {
      button.addEventListener('click', async () => {
        const orderId = button.getAttribute('data-save-order');
        const select = document.querySelector(`[data-order-status="${orderId}"]`);

        await apiFetch(`/api/admin/orders/${orderId}`, {
          method: 'PATCH',
          body: JSON.stringify({ status: select.value })
        });

        await refreshAdminData();
      });
    });
  }

  async function refreshAdminData() {
    const [statsResponse, productsResponse, ordersResponse] = await Promise.all([
      apiFetch('/api/admin/stats'),
      apiFetch('/api/products'),
      apiFetch('/api/admin/orders')
    ]);

    renderStats(statsResponse.stats);
    renderProducts(productsResponse.products || []);
    renderOrders(ordersResponse.orders || []);
    updateNav();
  }

  productForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData(productForm);
    const payload = {
      name: formData.get('name'),
      description: formData.get('description'),
      category: formData.get('category'),
      imageUrl: formData.get('imageUrl'),
      price: Number(formData.get('price')),
      stock: Number(formData.get('stock')),
      featured: formData.get('featured') === 'on'
    };

    const productId = productIdField.value;
    const method = productId ? 'PUT' : 'POST';
    const endpoint = productId ? `/api/products/${productId}` : '/api/products';

    await apiFetch(endpoint, {
      method,
      body: JSON.stringify(payload)
    });

    resetProductForm();
    await refreshAdminData();
  });

  // Load admin data on page load
  try {
    refreshAdminData();
  } catch (error) {
    statsGrid.innerHTML = `<div class="col-12"><div class="empty-state glass-card text-danger">${error.message}</div></div>`;
  }
});
