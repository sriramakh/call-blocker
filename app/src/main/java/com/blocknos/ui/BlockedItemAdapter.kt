package com.blocknos.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blocknos.R

data class BlockedItem(
    val id: Long,
    val title: String,
    val subtitle: String? = null
)

class BlockedItemAdapter(
    private val onDelete: (BlockedItem) -> Unit
) : ListAdapter<BlockedItem, BlockedItemAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BlockedItem>() {
            override fun areItemsTheSame(a: BlockedItem, b: BlockedItem) = a.id == b.id
            override fun areContentsTheSame(a: BlockedItem, b: BlockedItem) = a == b
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textItemTitle)
        val textSubtitle: TextView = view.findViewById(R.id.textItemSubtitle)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.textTitle.text = item.title
        if (item.subtitle != null) {
            holder.textSubtitle.text = item.subtitle
            holder.textSubtitle.visibility = View.VISIBLE
        } else {
            holder.textSubtitle.visibility = View.GONE
        }
        holder.buttonDelete.setOnClickListener { onDelete(item) }
    }
}
