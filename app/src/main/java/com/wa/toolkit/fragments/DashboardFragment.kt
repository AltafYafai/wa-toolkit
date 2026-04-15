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

import com.wa.toolkit.activities.MainActivity
import com.wa.toolkit.utils.ConfigUtil
import com.wa.toolkit.App
import com.wa.toolkit.xposed.core.FeatureLoader

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
            DashboardItem(1, getString(R.string.privacy), "Ghost Mode and Protection", R.drawable.ic_privacy),
            DashboardItem(3, getString(R.string.media), "HD Images, Video & Audio", R.drawable.ic_media),
            DashboardItem(2, "Chat", getString(R.string.summary_chat), R.drawable.ic_telegram),
            DashboardItem(8, getString(R.string.home_screen), getString(R.string.summary_home_screen), R.drawable.ic_home_black_24dp),
            DashboardItem(5, "Tools", getString(R.string.summary_tools), R.drawable.ic_general),
            DashboardItem(6, getString(R.string.status), "IG Style and Downloads", R.drawable.online),
            DashboardItem(7, getString(R.string.calls), "Privacy and Call Recording", R.drawable.ic_contacts)
        )

        val adapter = DashboardAdapter(items) { item ->
            listener?.onDashboardItemClick(item)
        }

        // Initialize status
        adapter.isXposedEnabled = MainActivity.isXposedEnabled()
        
        // Setup listeners
        (activity as? MainActivity)?.setOnStatusUpdateListener { version, active ->
            adapter.wppVersion = "WhatsApp $version"
            adapter.isWppActive = active
            adapter.notifyItemChanged(0) // Update header
        }

        adapter.onBackupClick = { ConfigUtil.exportConfigs(requireContext()) }
        adapter.onRestoreClick = { ConfigUtil.importConfigs(requireContext()) }
        adapter.onRestartClick = {
            App.getInstance().restartApp(FeatureLoader.PACKAGE_WPP)
            App.getInstance().restartApp(FeatureLoader.PACKAGE_BUSINESS)
        }

        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (recyclerView.adapter?.getItemViewType(position)) {
                    DashboardAdapter.TYPE_HEADER, DashboardAdapter.TYPE_ABOUT -> 2
                    else -> 1
                }
            }
        }

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        return view
    }
}
