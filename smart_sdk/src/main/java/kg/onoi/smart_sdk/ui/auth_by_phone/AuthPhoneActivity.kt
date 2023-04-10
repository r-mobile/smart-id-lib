package kg.onoi.smart_sdk.ui.auth_by_phone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.widget.textChanges
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.*
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.ui.auth_by_inn.MainInnActivity
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.registration_by_phone.RegistrationPhoneActivity
import kg.onoi.smart_sdk.utils.KgPhoneTextWatcher
import kg.onoi.smart_sdk.utils.SdkConfig
import kg.onoi.smart_sdk.utils.SdkHelper
import kg.onoi.smart_sdk.utils.SignType
import kotlinx.android.synthetic.main.activity_phone_auth.*
import kotlinx.android.synthetic.main.activity_phone_register.btn_next
import kotlinx.android.synthetic.main.activity_phone_register.cv_toolbar
import kotlinx.android.synthetic.main.activity_phone_register.set_phone

class AuthPhoneActivity :
    BaseActivity<AuthVM>(R.layout.activity_phone_auth, AuthVM::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        subscribeToLiveData()
    }

    private fun setupViews() {
        cv_toolbar.init(this, R.string.inn_toolbar) { closeActivityStack() }
        iv_logo.setImageDrawable(SdkConfig.logo)
        val textWatcher = KgPhoneTextWatcher(set_phone.getEditText())
        set_phone.addTextWatcher(textWatcher)
        set_phone.maxLength(textWatcher.numberLength)
        set_phone
            .getEditText()
            .textChanges()
            .subscribe { btn_next.isEnabled = it.length == textWatcher.numberLength }
        tv_change_phone.underline()
        tv_legal_entity.apply {
            setOnClickListener { goToCorporateSignIn() }
            setIsVisible(SdkHelper.signType == SignType.COMMON)
            underline()
        }
        btn_next.setOnClickListener { viewModel.sendConfirmSms(set_phone.getText()) }
    }

    private fun goToCorporateSignIn() {
        MainInnActivity.start(this)
        finish()
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is AuthByPhoneEvents.SuccessSmsSend -> {
                        AuthSmsConfirmActivity.start(this, it.phone)
                    }
                    is AuthByPhoneEvents.UserNotFound -> {
                        if(SdkConfig.enableRegistration) showRegistrationQuery()
                        else showToast("Номер ${set_phone.getText()} не зарегистрирован.")
                    }
                    is Event.Fail -> showToast(it.message)
                }
            }
        })
    }

    private fun showRegistrationQuery() {
        val number = set_phone.getText()
        showConfirmDialog(
            message = "Номер $number не зарегистрирован. Зарегистрироваться?",
            okLabel = getString(R.string.yes),
            cancelLabel = getString(R.string.no),
            onOkClick = { RegistrationPhoneActivity.start(this, number); finish()}
        )
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AuthPhoneActivity::class.java))
        }
    }
}
