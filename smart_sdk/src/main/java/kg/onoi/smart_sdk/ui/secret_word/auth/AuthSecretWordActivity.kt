package kg.onoi.smart_sdk.ui.secret_word.auth

import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.secret_word.BaseSecretWordActivity
import kg.onoi.smart_sdk.utils.ResponseWay
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_secret_word.*

class AuthSecretWordActivity :
    BaseSecretWordActivity<AuthSecretWordVM>(AuthSecretWordVM::class) {
    override fun onSaveBtnClick() {
        viewModel.checkSecretWord(SdkHelper.sessionId, set_secret_word.getText())
    }

    override fun handleSuccessProcess() {
        closeActivityStack()
        SdkHelper.actionCallback.invoke(SdkHelper.sessionId, ResponseWay.AUTH)
    }

    override fun isRegistration() = false

    companion object {
        fun start(activity: BaseActivity<*>) {
            activity.startActivity(activity.createIntent(AuthSecretWordActivity::class))
        }
    }
}