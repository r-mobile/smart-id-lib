package kg.onoi.smart_sdk.ui.secret_word.registration

import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.ui.passport_info.PassportInfoActivity
import kg.onoi.smart_sdk.ui.secret_word.BaseSecretWordActivity
import kotlinx.android.synthetic.main.activity_secret_word.*

class RegistrationSecretWordActivity :
    BaseSecretWordActivity<RegistrationSecretWordVM>(RegistrationSecretWordVM::class) {
    override fun onSaveBtnClick() {
        viewModel.saveSecretWord(user.sessionId!!, set_secret_word.getText())
    }

    override fun handleSuccessProcess() {
        PassportInfoActivity.start(this)
    }

    override fun isRegistration() = true

    companion object {
        fun start(activity: SimpleActivity) {
            activity.startActivity(activity.createIntent(RegistrationSecretWordActivity::class))
        }
    }
}