package com.example.cartmart.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.cartmart.R
import com.example.cartmart.core.formatCurrency
import com.example.cartmart.network.CartItemDto

class CartAdapter(
    private val onIncrease: (String) -> Unit,
    private val onDecrease: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    private val items = mutableListOf<CartItemDto>()

    fun submitList(newItems: List<CartItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.cartProductImage)
        private val productName: TextView = itemView.findViewById(R.id.cartProductName)
        private val productCategory: TextView = itemView.findViewById(R.id.cartProductCategory)
        private val productPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        private val quantityText: TextView = itemView.findViewById(R.id.cartQuantity)
        private val lineTotal: TextView = itemView.findViewById(R.id.cartLineTotal)
        private val increaseButton: Button = itemView.findViewById(R.id.cartIncreaseButton)
        private val decreaseButton: Button = itemView.findViewById(R.id.cartDecreaseButton)
        private val removeButton: Button = itemView.findViewById(R.id.cartRemoveButton)

        fun bind(item: CartItemDto) {
            productImage.load(item.imageUrl.ifBlank { null }) {
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
                crossfade(true)
            }
            productName.text = item.name
            productCategory.text = item.category
            productPrice.text = formatCurrency(item.price)
            quantityText.text = item.quantity.toString()
            lineTotal.text = formatCurrency(item.price * item.quantity)

            increaseButton.setOnClickListener { onIncrease(item.productId) }
            decreaseButton.setOnClickListener { onDecrease(item.productId) }
            removeButton.setOnClickListener { onRemove(item.productId) }
        }
    }
}
