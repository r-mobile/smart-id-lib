package kg.onoi.smart_sdk.custom_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kg.onoi.smart_sdk.R


class FullScreenDialog : androidx.fragment.app.DialogFragment() {

    private var onCancel: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AlertDialog_FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fullscreen_progress, container, false)
        return view
    }


    override fun onStart() {
        super.onStart()
        dialog?.apply {
            onCancel?.let { setOnCancelListener { it() } }
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(width, height)
        }

    }

    fun setOnCancelListener(onCancel: () -> Unit) {
        this.onCancel = onCancel
    }

}