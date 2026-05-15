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
import com.example.cartmart.network.ProductDto

class ProductAdapter(
    private val showActionButton: Boolean,
    private val onAddClick: ((ProductDto) -> Unit)?
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private val items = mutableListOf<ProductDto>()

    fun submitList(newItems: List<ProductDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView = itemView.findViewById(R.id.productImage)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productDescription: TextView = itemView.findViewById(R.id.productDescription)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val productStock: TextView = itemView.findViewById(R.id.productStock)
        private val actionButton: Button = itemView.findViewById(R.id.productActionButton)

        fun bind(product: ProductDto) {
            productName.text = product.name
            productDescription.text = product.description
            productPrice.text = formatCurrency(product.price)
            productStock.text = "Stock: ${product.stock}"
            productImage.load(product.imageUrl.ifBlank { null }) {
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
                crossfade(true)
            }
            actionButton.visibility = if (showActionButton) View.VISIBLE else View.GONE
            actionButton.isEnabled = product.stock > 0
            actionButton.text = if (product.stock > 0) "Add to Cart" else "Out of Stock"
            actionButton.setOnClickListener {
                if (product.stock > 0) {
                    onAddClick?.invoke(product)
                }
            }
        }
    }
}
