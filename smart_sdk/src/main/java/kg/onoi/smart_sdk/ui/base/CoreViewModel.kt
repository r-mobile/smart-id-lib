package kg.onoi.smart_sdk.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class CoreViewModel<Event> : ViewModel() {
    var event = MutableLiveData<Event>()
    var showProgress = MutableLiveData<Boolean>()

    val vmJob = Job()
    var vmCoroutineScope = CoroutineScope(Dispatchers.Main + vmJob)

    lateinit var exceptionHandler: ServerErrorHandler

    fun showProgress() = setProgressState(true)
    fun hideProgress() = setProgressState(false)

    private fun setProgressState(isShow: Boolean) {
        showProgress.value = isShow
    }

    fun runWithProgress(block: suspend () -> Unit) {
        showProgress()
        vmCoroutineScope.launch {
            try {
                block.invoke()
            } catch (e: Throwable) {
                exceptionHandler.handle(e)
            } finally {
                hideProgress()
            }
        }
    }

    fun safeCall(block: suspend () -> Unit) {
        vmCoroutineScope.launch {
            try {
                block.invoke()
            } catch (e: Throwable) {
                exceptionHandler.handle(e)
            }
        }
    }
}