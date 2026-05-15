document.addEventListener('DOMContentLoaded', () => {
  const cartMart = window.CartMart || {};
  const { apiFetch, addToCart, formatCurrency, updateNav } = cartMart;

  const productGrid = document.getElementById('productGrid');
  const searchInput = document.getElementById('searchInput');
  const categoryFilter = document.getElementById('categoryFilter');

  if (!productGrid || !searchInput || !categoryFilter || !apiFetch) {
    console.error('CartMartBD: Missing required elements or API');
    return;
  }

  let allProducts = [];

  function renderProducts(products) {
    if (!products.length) {
      productGrid.innerHTML = `
        <div class="col-12">
          <div class="empty-state glass-card">
            <h3 class="mb-2">No products found</h3>
            <p class="mb-0">Try a different search term or category.</p>
          </div>
        </div>
      `;
      return;
    }

    productGrid.innerHTML = products.map((product) => `
      <div class="col-md-6 col-lg-4 fade-in">
        <article class="product-card h-100 overflow-hidden">
          <div class="product-image-wrap">
            <img src="${product.imageUrl}" alt="${product.name}">
            <span class="product-badge">${product.category}</span>
          </div>
          <div class="product-card-body p-3 p-lg-4">
            <div class="product-card-top">
              <div class="flex-grow-1">
                <h3 class="h5 mb-1">${product.name}</h3>
                <p class="text-soft small mb-0">${product.description}</p>
              </div>
              <button class="btn btn-accent btn-sm product-cta" data-add-product="${product._id}" ${product.stock <= 0 ? 'disabled' : ''}>
                ${product.stock > 0 ? 'Add to Cart' : 'Out of Stock'}
              </button>
            </div>
            <div class="product-meta d-flex align-items-center justify-content-between mt-3">
              <span class="fs-4 fw-bold">${formatCurrency(product.price)}</span>
              <span class="text-soft small">Stock: ${product.stock}</span>
            </div>
          </div>
        </article>
      </div>
    `).join('');

    document.querySelectorAll('[data-add-product]').forEach((button) => {
      button.addEventListener('click', () => {
        const product = allProducts.find((item) => item._id === button.getAttribute('data-add-product'));
        if (product) {
          addToCart(product);
          updateNav();
          button.textContent = 'Added';
          button.disabled = true;
        }
      });
    });
  }

  function applyFilters() {
    const query = searchInput.value.trim().toLowerCase();
    const category = categoryFilter.value;

    const filtered = allProducts.filter((product) => {
      const matchesSearch = !query || [product.name, product.description, product.category]
        .join(' ')
        .toLowerCase()
        .includes(query);
      const matchesCategory = !category || product.category === category;
      return matchesSearch && matchesCategory;
    });

    renderProducts(filtered);
  }

  async function loadProducts() {
    const response = await apiFetch('/api/products');
    allProducts = response.products || [];

    const categories = [...new Set(allProducts.map((product) => product.category))];
    categoryFilter.innerHTML = `<option value="">All categories</option>${categories
      .map((category) => `<option value="${category}">${category}</option>`)
      .join('')}`;

    renderProducts(allProducts);
  }

  searchInput.addEventListener('input', applyFilters);
  categoryFilter.addEventListener('change', applyFilters);

  loadProducts().catch((error) => {
    console.error('Failed to load products:', error);
    productGrid.innerHTML = `
      <div class="col-12">
        <div class="empty-state glass-card text-danger">
          <h3 class="mb-2">Unable to load products</h3>
          <p class="mb-0">${error.message || 'Unknown error'}</p>
        </div>
      </div>
    `;
  });
});
