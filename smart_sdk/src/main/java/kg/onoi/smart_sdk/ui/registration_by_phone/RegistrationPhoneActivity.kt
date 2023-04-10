package kg.onoi.smart_sdk.ui.registration_by_phone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.widget.textChanges
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.showToast
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.utils.KgPhoneTextWatcher
import kg.onoi.smart_sdk.utils.SdkConfig
import kotlinx.android.synthetic.main.activity_phone_register.*
import org.parceler.Parcels

class RegistrationPhoneActivity :
    BaseActivity<RegistrationVM>(R.layout.activity_phone_register, RegistrationVM::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        subscribeToLiveData()
    }

    @SuppressLint("CheckResult")
    private fun setupViews() {
        cv_toolbar.init(this, R.string.registration) { closeActivityStack() }
        iv_logo.setImageDrawable(SdkConfig.logo)
        val phoneTextWatcher = KgPhoneTextWatcher(set_phone.getEditText())
        set_phone.addTextWatcher(phoneTextWatcher)
        set_phone.maxLength(phoneTextWatcher.numberLength)
        set_phone
            .getEditText()
            .textChanges()
            .subscribe { btn_next.isEnabled = it.length == phoneTextWatcher.numberLength }
        btn_next.setOnClickListener { viewModel.sendConfirmSms(set_phone.getText()) }
        setupPhoneFromIntent()
    }

    private fun setupPhoneFromIntent() {
        if (!intent.hasExtra(Constant.Extra.PHONE)) return
        intent.getStringExtra(Constant.Extra.PHONE)
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                set_phone.getEditText().apply {
                    setText(it)
                    setSelection(it.length)
                }
                viewModel.sendConfirmSms(set_phone.getText())
            }
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is PhoneRegistrationEvents.SuccessSmsSend -> {
                        showNextActivity(it.sessionId)
                        RegistrationSmsConfirmationActivity.start(this)
                    }
                    is Event.Fail -> showToast(it.message)
                }
            }
        })
    }

    private fun showNextActivity(id: String) {
        user.apply {
            sessionId = id
            phoneNumber = set_phone.getText()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        fun start(context: Context, phone: String? = null) {
            context.startActivity(Intent(context, RegistrationPhoneActivity::class.java).apply {
                putExtra(Constant.Extra.USER, Parcels.wrap(User()))
                phone?.let { putExtra(Constant.Extra.PHONE, it) }
            })
        }
    }
}
