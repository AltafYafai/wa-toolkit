package com.wa.toolkit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wa.toolkit.R
import com.wa.toolkit.adapter.DashboardAdapter
import com.wa.toolkit.adapter.DashboardItem

class DashboardFragment : Fragment() {

    interface OnDashboardItemClickListener {
        fun onDashboardItemClick(item: DashboardItem)
    }

    private var listener: OnDashboardItemClickListener? = null

    fun setOnDashboardItemClickListener(listener: OnDashboardItemClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)

        val items = listOf(
            DashboardItem(1, getString(R.string.privacy), R.drawable.ic_privacy),
            DashboardItem(3, getString(R.string.media), R.drawable.ic_media),
            DashboardItem(2, "Chat", R.drawable.ic_telegram),
            DashboardItem(4, getString(R.string.perso), R.drawable.ic_dashboard_black_24dp),
            DashboardItem(5, "Tools", R.drawable.ic_general),
            DashboardItem(6, getString(R.string.status), R.drawable.online),
            DashboardItem(7, getString(R.string.calls), R.drawable.ic_contacts)
        )

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = DashboardAdapter(items) { item ->
            listener?.onDashboardItemClick(item)
        }

        return view
    }
}
