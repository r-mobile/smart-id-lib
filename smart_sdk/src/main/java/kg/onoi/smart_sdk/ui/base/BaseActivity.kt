package kg.onoi.smart_sdk.ui.base

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.LayoutRes
import kg.onoi.smart_sdk.custom_view.FullScreenDialog
import kg.onoi.smart_sdk.repositories.ViewModelFactory
import kotlinx.coroutines.cancelChildren
import kotlin.reflect.KClass

abstract class BaseActivity<T : ViewModel>(@LayoutRes override val layoutRes: Int, val vmClass: KClass<T>) :
    SimpleActivity(layoutRes) {

    private val CLOSE_ACTION = "kg.onoi.smart_registration.CLOSE_ACTION"
    lateinit var viewModel: T
    private lateinit var exceptionHandler: ServerErrorHandler
    private var alertDialog: FullScreenDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exceptionHandler = ServerErrorHandler(this)
        viewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(vmClass.java)
        setupCloseReceiver()
        setContentView(layoutRes)
        subscribeToViewModelLiveData()
    }

    override fun onDestroy() {
        (viewModel as? CoreViewModel<*>)?.vmJob?.cancel()
        super.onDestroy()
    }

    private fun subscribeToViewModelLiveData() {
        (viewModel as? CoreViewModel<*>)?.let {
            it.showProgress.observe(this, Observer {
                when (it) {
                    true -> showProgressDialog()
                    else -> hideProgressDialog()
                }
            })

            it.exceptionHandler = exceptionHandler
        }
    }

    private fun showProgressDialog() {
        if (alertDialog != null) {
            alertDialog?.dismiss()
            alertDialog = null
        }

        alertDialog = FullScreenDialog()
        alertDialog?.setOnCancelListener { (viewModel as? CoreViewModel<*>)?.vmJob?.cancelChildren() }
        alertDialog?.show(supportFragmentManager, "")
    }

    private fun hideProgressDialog() {
        alertDialog?.dismiss()
        alertDialog = null
    }
}
