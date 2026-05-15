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
