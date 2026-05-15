# CartMartBD

CartMartBD is a minimal eCommerce website built with plain HTML, CSS, JavaScript, Node.js, Express.js, and MongoDB. It supports two roles:

- **user**: browse products, manage a cart, place orders, and view order history
- **admin**: create, update, and delete products, plus review and update orders

## Features

- Vanilla frontend with Bootstrap styling
- JWT-based authentication
- Role-based authorization
- Product catalog stored in MongoDB
- Cart stored in the browser for simplicity
- Orders saved in MongoDB
- Seed script that creates a default admin and sample products

## Default admin

- Email: `admin@cartmartbd.com`
- Password: `Admin@12345`

## Setup

1. Install dependencies:

   ```bash
   npm install
   ```

2. Create a `.env` file from `.env.example` and set your MongoDB URI and JWT secret.

3. Start MongoDB locally or point to a hosted MongoDB instance.

4. Seed the database:

   ```bash
   npm run seed
   ```

5. Start the app:

   ```bash
   npm run dev
   ```

6. Open `http://localhost:5000`

## Project structure

- `server.js` - app entry point
- `config/` - database connection
- `models/` - MongoDB models
- `routes/` - API routes
- `middleware/` - auth and role guards
- `public/` - frontend pages, styles, and scripts
- `utils/seed.js` - creates demo data

## Notes

This is intentionally minimal and uses browser storage for the shopping cart. If you want, the cart can be moved to MongoDB or sessions later without changing the overall structure.

## Android app API

Use these JSON APIs from an Android client. All authenticated requests must send:

```http
Authorization: Bearer <token>
Content-Type: application/json
```

### Auth

`POST /api/auth/register`

Request body:

```json
{
   "name": "Test Customer",
   "email": "testcustomer@example.com",
   "password": "TestPass@123"
}
```

`POST /api/auth/login`

Request body:

```json
{
   "email": "admin@cartmartbd.com",
   "password": "Admin@12345"
}
```

`GET /api/auth/me`

Returns the currently authenticated user.

### Products

`GET /api/products`

Optional query params:

- `search` - search by name, description, or category
- `category` - exact category filter
- `featured=true` - only featured products

`GET /api/products/:id`

Returns one product by id.

`POST /api/products`

Admin only. Creates a product.

`PUT /api/products/:id`

Admin only. Updates a product.

`DELETE /api/products/:id`

Admin only. Deletes a product.

### Orders

`POST /api/orders`

Place a new order.

Request body:

```json
{
   "items": [
      { "productId": "<productId>", "quantity": 1 }
   ],
   "shippingName": "Test Customer",
   "shippingPhone": "01234567890",
   "shippingAddress": "123 Test Street, Test City",
   "paymentMethod": "Cash on delivery"
}
```

`GET /api/orders/mine`

Returns the signed-in user's order history.

`GET /api/orders/:id`

Returns a single order. The owner or an admin can access it.

### Admin

`GET /api/admin/stats`

Returns product, order, user, and pending-order counts.

`GET /api/admin/orders`

Returns all orders with customer and item details.

`PATCH /api/admin/orders/:id`

Updates order status. Allowed values: `Pending`, `Processing`, `Shipped`, `Delivered`, `Cancelled`.

## Test data

Run `npm run seed` to create the default admin and sample products.

### Default admin

- Email: `admin@cartmartbd.com`
- Password: `Admin@12345`

### Seeded products

The seed script loads these sample products when the database is empty:

- Wireless Headphones - Electronics - $89.99 - stock 25
- Minimal Backpack - Accessories - $49.99 - stock 40
- Running Shoes - Fashion - $74.99 - stock 30
- Smart Watch - Electronics - $119.99 - stock 18

### Suggested Android test flow

1. Log in with the default admin account.
2. Fetch the product list using `GET /api/products`.
3. Pick one product id and place an order with `POST /api/orders`.
4. Open `GET /api/orders/mine` to confirm the order appears.
5. Log in as admin and open `GET /api/admin/orders` to confirm item details and status updates.
