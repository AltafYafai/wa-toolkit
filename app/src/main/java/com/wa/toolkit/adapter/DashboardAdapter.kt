package com.wa.toolkit.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wa.toolkit.R
import com.wa.toolkit.activities.AboutActivity
import com.wa.toolkit.activities.SearchActivity
import com.wa.toolkit.utils.HapticUtil

data class DashboardItem(val id: Int, val title: String, val icon: Int)

class DashboardAdapter(
    private val items: List<DashboardItem>,
    private val onItemClick: (DashboardItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        const val TYPE_ABOUT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            items.size + 1 -> TYPE_ABOUT
            else -> TYPE_ITEM
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.cardSearch)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.cardView)
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)
    }

    class AboutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.cardAbout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_dashboard_header, parent, false))
            TYPE_ABOUT -> AboutViewHolder(inflater.inflate(R.layout.item_dashboard_about, parent, false))
            else -> ItemViewHolder(inflater.inflate(R.layout.item_dashboard_card, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.card.setOnClickListener {
                    HapticUtil.playClick(it.context)
                    it.context.startActivity(Intent(it.context, SearchActivity::class.java))
                }
            }
            is AboutViewHolder -> {
                holder.card.setOnClickListener {
                    HapticUtil.playClick(it.context)
                    it.context.startActivity(Intent(it.context, AboutActivity::class.java))
                }
            }
            is ItemViewHolder -> {
                val item = items[position - 1]
                holder.title.text = item.title
                holder.icon.setImageResource(item.icon)
                holder.card.setOnClickListener {
                    HapticUtil.playClick(it.context)
                    onItemClick(item)
                }
            }
        }
    }

    override fun getItemCount() = items.size + 2 // +Header +About
}
