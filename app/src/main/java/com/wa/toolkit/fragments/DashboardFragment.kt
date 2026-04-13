package com.wa.toolkit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
            DashboardItem(0, getString(R.string.general), "General tweaks & settings", R.drawable.ic_general),
            DashboardItem(1, getString(R.string.privacy), "Ghost mode & privacy", R.drawable.ic_privacy),
            DashboardItem(2, getString(R.string.title_home), "Home screen actions", R.drawable.ic_home_black_24dp),
            DashboardItem(3, getString(R.string.media), "High quality & downloads", R.drawable.ic_media),
            DashboardItem(4, getString(R.string.perso), "Themes & customization", R.drawable.ic_dashboard_black_24dp),
            DashboardItem(5, getString(R.string.recordings_manager), "Call recordings", R.drawable.ic_recording)
        )

        recyclerView.adapter = DashboardAdapter(items) { item ->
            listener?.onDashboardItemClick(item)
        }

        return view
    }
}
