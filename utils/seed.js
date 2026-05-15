const bcrypt = require('bcryptjs');
const User = require('../models/User');
const Product = require('../models/Product');

async function seedDatabase() {
  const adminEmail = 'admin@cartmartbd.com';
  const existingAdmin = await User.findOne({ email: adminEmail });

  if (!existingAdmin) {
    const password = await bcrypt.hash('Admin@12345', 10);
    await User.create({
      name: 'CartMart Admin',
      email: adminEmail,
      password,
      role: 'admin'
    });
  }

  const productCount = await Product.countDocuments();

  if (productCount === 0) {
    await Product.insertMany([
      {
        name: 'Wireless Headphones',
        description: 'Comfortable over-ear headphones with rich sound and long battery life.',
        category: 'Electronics',
        imageUrl: 'https://images.unsplash.com/photo-1518444028785-8f6f5b6f5c48?auto=format&fit=crop&w=1200&q=80',
        price: 89.99,
        stock: 25,
        featured: true
      },
      {
        name: 'Minimal Backpack',
        description: 'A durable everyday backpack with a clean design and laptop sleeve.',
        category: 'Accessories',
        imageUrl: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1200&q=80',
        price: 49.99,
        stock: 40,
        featured: true
      },
      {
        name: 'Running Shoes',
        description: 'Lightweight shoes designed for daily runs and all-day comfort.',
        category: 'Fashion',
        imageUrl: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80',
        price: 74.99,
        stock: 30,
        featured: false
      },
      {
        name: 'Smart Watch',
        description: 'Track your activity, messages, and calls with a bright display.',
        category: 'Electronics',
        imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1200&q=80',
        price: 119.99,
        stock: 18,
        featured: true
      }
    ]);
  }
}

module.exports = seedDatabase;

if (require.main === module) {
  require('dotenv').config();
  const connectDB = require('../config/db');

  connectDB()
    .then(seedDatabase)
    .then(() => {
      console.log('Seed complete');
      process.exit(0);
    })
    .catch((error) => {
      console.error('Seed failed:', error);
      process.exit(1);
    });
}
