package com.wa.toolkit.utils

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

object FilePicker {

    private var mOnFilePickedListener: OnFilePickedListener? = null
    private var mActivity: AppCompatActivity? = null
    @JvmField var fileSalve: ActivityResultLauncher<String>? = null
    private var mOnUriPickedListener: OnUriPickedListener? = null
    @JvmField var fileCapture: ActivityResultLauncher<Array<String>>? = null
    @JvmField var directoryCapture: ActivityResultLauncher<Uri>? = null
    @JvmField var imageCapture: ActivityResultLauncher<PickVisualMediaRequest>? = null

    @JvmStatic
    fun registerFilePicker(activity: AppCompatActivity) {
        mActivity = activity
        fileCapture = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { setFile(it) }
        imageCapture = activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { setFile(it) }
        directoryCapture = activity.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { setDirectory(it) }
        fileSalve = activity.registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { setFile(it) }
    }

    private fun setFile(uri: Uri?) {
        if (uri == null) return

        mOnUriPickedListener?.let {
            it.onUriPicked(uri)
            mOnUriPickedListener = null
        }

        mOnFilePickedListener?.let { listener ->
            var realPath: String? = null
            try {
                realPath = RealPathUtil.getRealFilePath(mActivity, uri)
            } catch (ignored: Exception) {
            }
            if (realPath == null) return
            listener.onFilePicked(File(realPath))
            mOnFilePickedListener = null
        }
    }

    private fun setDirectory(uri: Uri?) {
        if (uri == null) return

        if (mOnFilePickedListener == null) {
            mOnUriPickedListener?.onUriPicked(uri)
            mOnUriPickedListener = null
        }

        mOnFilePickedListener?.let { listener ->
            var realPath: String? = null
            try {
                realPath = RealPathUtil.getRealFolderPath(mActivity, uri)
            } catch (ignored: Exception) {
            }
            if (realPath == null) return
            listener.onFilePicked(File(realPath))
            mOnFilePickedListener = null
        }
    }

    @JvmStatic
    fun setOnFilePickedListener(onFilePickedListener: OnFilePickedListener?) {
        mOnFilePickedListener = onFilePickedListener
        mOnUriPickedListener = null
    }

    @JvmStatic
    fun setOnUriPickedListener(onUriPickedListener: OnUriPickedListener?) {
        mOnUriPickedListener = onUriPickedListener
        mOnFilePickedListener = null
    }

    interface OnFilePickedListener {
        fun onFilePicked(file: File)
    }

    interface OnUriPickedListener {
        fun onUriPicked(uri: Uri)
    }
}
