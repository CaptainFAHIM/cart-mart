const express = require('express');
const Product = require('../models/Product');
const { protect, requireRole } = require('../middleware/auth');

const router = express.Router();

router.get('/', async (req, res) => {
  try {
    const { search = '', category = '', featured = '' } = req.query;
    const filter = {};

    if (search) {
      const escaped = search.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      filter.$or = [
        { name: { $regex: escaped, $options: 'i' } },
        { description: { $regex: escaped, $options: 'i' } },
        { category: { $regex: escaped, $options: 'i' } }
      ];
    }

    if (category) {
      filter.category = category;
    }

    if (featured === 'true') {
      filter.featured = true;
    }

    const products = await Product.find(filter).sort({ createdAt: -1 });
    res.json({ products });
  } catch (error) {
    res.status(500).json({ message: 'Failed to load products' });
  }
});

router.get('/:id', async (req, res) => {
  try {
    const product = await Product.findById(req.params.id);

    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }

    res.json({ product });
  } catch (error) {
    res.status(400).json({ message: 'Failed to load product' });
  }
});

router.post('/', protect, requireRole('admin'), async (req, res) => {
  try {
    const { _id, id, ...productData } = req.body;
    const product = await Product.create(productData);
    res.status(201).json({ product });
  } catch (error) {
    res.status(400).json({ message: 'Failed to create product' });
  }
});

router.put('/:id', protect, requireRole('admin'), async (req, res) => {
  try {
    const { _id, id, ...productData } = req.body;
    const product = await Product.findByIdAndUpdate(req.params.id, productData, {
      new: true,
      runValidators: true
    });

    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }

    res.json({ product });
  } catch (error) {
    res.status(400).json({ message: 'Failed to update product' });
  }
});

router.delete('/:id', protect, requireRole('admin'), async (req, res) => {
  try {
    const product = await Product.findByIdAndDelete(req.params.id);
    if (!product) {
      return res.status(404).json({ message: 'Product not found' });
    }

    res.json({ message: 'Product deleted' });
  } catch (error) {
    res.status(500).json({ message: 'Failed to delete product' });
  }
});

module.exports = router;
