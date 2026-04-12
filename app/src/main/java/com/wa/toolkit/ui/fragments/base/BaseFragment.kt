package com.wa.toolkit.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.wa.toolkit.App
import com.wa.toolkit.R
import com.wa.toolkit.databinding.BaseFragmentBinding
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

open class BaseFragment : Fragment() {

    private var _binding: BaseFragmentBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = BaseFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun navigateUp() {
        navController.navigateUp()
    }

    val navController: NavController
        get() = NavHostFragment.findNavController(this)

    fun safeNavigate(@IdRes resId: Int): Boolean {
        return try {
            navController.navigate(resId)
            true
        } catch (ignored: IllegalArgumentException) {
            false
        }
    }

    fun safeNavigate(direction: NavDirections): Boolean {
        return try {
            navController.navigate(direction)
            true
        } catch (ignored: IllegalArgumentException) {
            false
        }
    }

    @JvmOverloads
    fun setupToolbar(toolbar: Toolbar, tipsView: View?, title: Int, menu: Int = -1) {
        setupToolbar(toolbar, tipsView, getString(title), menu, null)
    }

    @JvmOverloads
    fun setupToolbar(toolbar: Toolbar, tipsView: View?, title: String, menu: Int = -1, navigationOnClickListener: View.OnClickListener? = null) {
        toolbar.setNavigationOnClickListener(navigationOnClickListener ?: View.OnClickListener { navigateUp() })
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.title = title
        toolbar.tooltipText = title
        tipsView?.tooltipText = title
        if (menu != -1) {
            toolbar.inflateMenu(menu)
            if (this is MenuProvider) {
                toolbar.setOnMenuItemClickListener { item ->
                    this.onMenuItemSelected(item)
                }
                this.onPrepareMenu(toolbar.menu)
            }
        }
    }

    fun runAsync(runnable: Runnable) {
        App.getExecutorService().submit(runnable)
    }

    fun <T> runAsync(callable: Callable<T>): Future<T> {
        return App.getExecutorService().submit(callable)
    }

    fun runOnUiThread(runnable: Runnable) {
        App.getMainHandler().post(runnable)
    }

    fun <T> runOnUiThread(callable: Callable<T>): Future<T> {
        val task = FutureTask(callable)
        runOnUiThread(task)
        return task
    }

    @JvmOverloads
    fun showHint(@StringRes res: Int, lengthShort: Boolean, @StringRes actionRes: Int = -1, action: View.OnClickListener? = null) {
        val actionStr = if (actionRes != -1) App.getInstance().getString(actionRes) else null
        showHint(App.getInstance().getString(res), lengthShort, actionStr, action)
    }

    @JvmOverloads
    fun showHint(str: CharSequence, lengthShort: Boolean, actionStr: CharSequence? = null, action: View.OnClickListener? = null) {
        val container = view
        if (isResumed && container != null) {
            val snackbar = Snackbar.make(container, str, if (lengthShort) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_LONG)
            if (actionStr != null && action != null) snackbar.setAction(actionStr, action)
            snackbar.show()
            return
        }
        runOnUiThread {
            try {
                Toast.makeText(App.getInstance(), str, if (lengthShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
            } catch (ignored: Throwable) {
            }
        }
    }

    fun setDisplayHomeAsUpEnabled(enabled: Boolean) {
        val activity = activity as? AppCompatActivity ?: return
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }
}
