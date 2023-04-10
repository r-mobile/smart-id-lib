package kg.onoi.smart_sdk.ui.close

import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.utils.ExtrasName
import kg.onoi.smart_sdk.utils.SdkHelper

class CloseSessionActivity :
    BaseActivity<CloseSessionVM>(R.layout.activity_close_session, CloseSessionVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeLiveData()
        viewModel.closeSession(getSessionId())
    }

    private fun subscribeLiveData() {
        viewModel.event.observe(this, Observer {
            SdkHelper.logoutCallback.invoke(); finish()
        })
    }

    private fun getSessionId() = intent.getStringExtra(ExtrasName.SESSION_ID)

    companion object {
        fun start(context: Context, sessionId: String, callback: () -> Unit) {
            SdkHelper.logoutCallback = callback
            context.startActivity(Intent(context, CloseSessionActivity::class.java).apply {
                putExtra(ExtrasName.SESSION_ID, sessionId)
            })
        }
    }
}