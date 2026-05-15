const express = require('express');
const Order = require('../models/Order');
const Product = require('../models/Product');
const User = require('../models/User');
const { protect, requireRole } = require('../middleware/auth');

const router = express.Router();

router.use(protect, requireRole('admin'));

router.get('/stats', async (req, res) => {
  try {
    const [productCount, orderCount, userCount, pendingOrders] = await Promise.all([
      Product.countDocuments(),
      Order.countDocuments(),
      User.countDocuments(),
      Order.countDocuments({ status: 'Pending' })
    ]);

    res.json({
      stats: {
        productCount,
        orderCount,
        userCount,
        pendingOrders
      }
    });
  } catch (error) {
    res.status(500).json({ message: 'Failed to load admin stats' });
  }
});

router.get('/orders', async (req, res) => {
  try {
    const orders = await Order.find()
      .populate('user', 'name email role')
      .populate('items.product', 'name imageUrl')
      .sort({ createdAt: -1 });

    res.json({ orders });
  } catch (error) {
    res.status(500).json({ message: 'Failed to load orders' });
  }
});

router.patch('/orders/:id', async (req, res) => {
  try {
    const { status } = req.body;
    const allowedStatuses = ['Pending', 'Processing', 'Shipped', 'Delivered', 'Cancelled'];

    if (!allowedStatuses.includes(status)) {
      return res.status(400).json({ message: 'Invalid order status' });
    }

    const order = await Order.findByIdAndUpdate(
      req.params.id,
      { status },
      { new: true, runValidators: true }
    )
      .populate('user', 'name email role')
      .populate('items.product', 'name imageUrl');

    if (!order) {
      return res.status(404).json({ message: 'Order not found' });
    }

    res.json({ order });
  } catch (error) {
    res.status(500).json({ message: 'Failed to update order' });
  }
});

module.exports = router;
