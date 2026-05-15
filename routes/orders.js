const express = require('express');
const Product = require('../models/Product');
const Order = require('../models/Order');
const { protect, requireRole } = require('../middleware/auth');

const router = express.Router();

router.post('/', protect, requireRole('user', 'admin'), async (req, res) => {
  try {
    const { items, shippingName, shippingPhone, shippingAddress, paymentMethod } = req.body;

    if (!Array.isArray(items) || items.length === 0) {
      return res.status(400).json({ message: 'Cart is empty' });
    }

    if (!shippingName || !shippingPhone || !shippingAddress) {
      return res.status(400).json({ message: 'Shipping details are required' });
    }

    const normalizedItems = items
      .map((item) => ({
        productId: item.productId,
        quantity: Number(item.quantity || 1)
      }))
      .filter((item) => item.productId && item.quantity > 0);

    if (normalizedItems.length === 0) {
      return res.status(400).json({ message: 'Cart is empty' });
    }

    const productIds = normalizedItems.map((item) => item.productId);
    const products = await Product.find({ _id: { $in: productIds } });

    const orderItems = [];
    let totalPrice = 0;

    for (const item of normalizedItems) {
      const product = products.find((entry) => entry._id.toString() === item.productId);
      if (!product) {
        return res.status(400).json({ message: 'One or more products no longer exist' });
      }

      if (product.stock < item.quantity) {
        return res.status(400).json({ message: `Not enough stock for ${product.name}` });
      }

      orderItems.push({
        product: product._id,
        name: product.name,
        price: product.price,
        quantity: item.quantity
      });

      totalPrice += product.price * item.quantity;
    }

    for (const item of normalizedItems) {
      const product = products.find((entry) => entry._id.toString() === item.productId);
      product.stock -= item.quantity;
      await product.save();
    }

    const order = await Order.create({
      user: req.user._id,
      items: orderItems,
      shippingName,
      shippingPhone,
      shippingAddress,
      paymentMethod: paymentMethod || 'Cash on delivery',
      totalPrice
    });

    const populatedOrder = await Order.findById(order._id)
      .populate('user', 'name email role')
      .populate('items.product', 'name imageUrl');

    res.status(201).json({ order: populatedOrder });
  } catch (error) {
    res.status(500).json({ message: 'Failed to place order' });
  }
});

router.get('/mine', protect, requireRole('user', 'admin'), async (req, res) => {
  try {
    const orders = await Order.find({ user: req.user._id })
      .populate('user', 'name email role')
      .populate('items.product', 'name imageUrl')
      .sort({ createdAt: -1 });

    res.json({ orders });
  } catch (error) {
    res.status(500).json({ message: 'Failed to load your orders' });
  }
});

router.get('/:id', protect, requireRole('user', 'admin'), async (req, res) => {
  try {
    const order = await Order.findById(req.params.id)
      .populate('user', 'name email role')
      .populate('items.product', 'name imageUrl');

    if (!order) {
      return res.status(404).json({ message: 'Order not found' });
    }

    const isOwner = order.user._id.toString() === req.user._id.toString();
    const isAdmin = req.user.role === 'admin';

    if (!isOwner && !isAdmin) {
      return res.status(403).json({ message: 'Access denied' });
    }

    res.json({ order });
  } catch (error) {
    res.status(400).json({ message: 'Failed to load order' });
  }
});

module.exports = router;
