package kg.onoi.smart_sdk.ui.auth_by_inn

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.widget.textChanges
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.*
import kg.onoi.smart_sdk.ui.auth_by_phone.AuthPhoneActivity
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.registration_by_phone.RegistrationPhoneActivity
import kg.onoi.smart_sdk.utils.Consts
import kg.onoi.smart_sdk.utils.InnTextWatcher
import kg.onoi.smart_sdk.utils.SdkConfig
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.btn_next
import kotlinx.android.synthetic.main.activity_sign_in.cv_toolbar
import kotlinx.android.synthetic.main.activity_sign_in.iv_logo

abstract class BaseInnActivity :
    BaseActivity<InnInputVM>(R.layout.activity_sign_in, InnInputVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cv_toolbar.init(this, R.string.inn_toolbar)
        isAllowBackAndHome = true
        initViews()
    }

    open fun initViews() {
        iv_logo.setImageDrawable(SdkConfig.logo)
        set_inn
            .getEditText()
            .addTextChangedListener(InnTextWatcher(set_inn.getEditText(), this))

        tv_individual_entity.apply {
            underline()
            setOnClickListener { AuthPhoneActivity.start(context); finish() }
        }

        set_inn
            .getEditText()
            .textChanges()
            .map { resetInputLayout(); it }
            .map { it.length == Consts.INN_LENGTH }
            .subscribe {
                btn_next.isEnabled = it
                if (it) viewModel.requestNameByInn(set_inn.getText())
            }
    }

    fun setName(name: String) {
        tv_name.apply {
            text = name
            setTextColor(ContextCompat.getColor(context, R.color.sm_control_color))
            visible()
        }
    }

    fun setInvalidState(message: String) {
        tv_name.apply {
            text = message
            setTextColor(ContextCompat.getColor(context, R.color.md_red800))
            visible()
        }
    }

    private fun resetInputLayout() {
        tv_name.apply {
            text = null
            invisible()
        }
    }

    protected fun showRegistrationQuery(message: String) {
        showConfirmDialog(
            message = message,
            okLabel = getString(R.string.yes),
            cancelLabel = getString(R.string.no),
            onOkClick = { RegistrationPhoneActivity.start(this); finish()}
        )
    }
}
