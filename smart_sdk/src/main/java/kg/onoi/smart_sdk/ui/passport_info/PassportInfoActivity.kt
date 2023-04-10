package kg.onoi.smart_sdk.ui.passport_info

import android.app.Activity
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.jakewharton.rxbinding3.widget.textChanges
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.custom_view.StatedEditText
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.RecognitionInfo
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.ui.passport_tutorial.PassportTutorialActivity
import kg.onoi.smart_sdk.ui.registration_confirmation.RegistrationConfirmationActivity
import kg.onoi.smart_sdk.utils.InnTextWatcher
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_recognition.*
import kotlinx.android.synthetic.main.activity_recognition.btn_next
import kotlinx.android.synthetic.main.activity_recognition.cv_toolbar
import java.text.SimpleDateFormat

class PassportInfoActivity : BaseActivity<PassportInfoVM>(
    R.layout.activity_recognition,
    PassportInfoVM::class
) {
    private var userDidNotChangeData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupUser(user)
        viewModel.updateInfoFromRecognition()
        setupViews()
        subscribeToLiveData()
    }

    override fun onBackPressed() {
        if (isStartForModeration()) finish()
        else super.onBackPressed()
    }

    private fun setupViews() {
        if (isStartForModeration()) cv_toolbar.init(this, R.string.passport_data) { finish() }
        else cv_toolbar.init(this, R.string.passport_data)

        set_inn.addTextWatcher(InnTextWatcher(set_inn.getEditText(), this))
        set_inn.addEndIcon(R.drawable.ic_help) { PassportTutorialActivity.start(this) }
        set_inn.getEditText().textChanges().subscribe { onTextChanged(set_inn) }
        set_passport.getEditText().textChanges().subscribe { onTextChanged(set_passport) }
        set_surname.getEditText().textChanges().subscribe { onTextChanged(set_surname) }
        set_name.getEditText().textChanges().subscribe { onTextChanged(set_name) }
        set_patronymic.getEditText().textChanges().subscribe { onTextChanged(set_patronymic) }
        set_date_birth.getEditText().textChanges().subscribe { onTextChanged(set_date_birth) }
        set_date_issue.getEditText().textChanges().subscribe { onTextChanged(set_date_issue) }
        set_date_expiry.getEditText().textChanges().subscribe { onTextChanged(set_date_expiry) }
        set_authority.getEditText().textChanges().subscribe { onTextChanged(set_authority) }
        set_address.apply {
            isVisible = SdkHelper.settings.needFillAddress
            getEditText().textChanges().subscribe { onTextChanged(set_address) }
        }

        btn_next.setOnClickListener { onNextClick() }
        btn_cancel.setOnClickListener { showFinishRegisterProcessQuery() }

        updateNextButton()
    }

    private fun onTextChanged(input: StatedEditText) {
        input.hideError()
        userDidNotChangeData = false
        updateNextButton()
    }

    private fun updateNextButton() {
        btn_next.isEnabled = when {
            set_passport.length() < Constant.Length.PASSPORT_NUMBER -> {
                set_passport.showError("Некорретный номер паспорта")
                false

            }
            set_inn.length() < Constant.Length.INN -> {
                set_inn.showError("Некорретный ИНН")
                false
            }
            set_surname.length() < Constant.Length.NAME -> {
                set_surname.showError("Некорретная фамилия")
                false
            }
            set_name.length() < Constant.Length.NAME -> {
                set_name.showError("Некорретное имя")
                false
            }
            !isValidDate(set_date_birth.getText()) -> {
                set_date_birth.showError("Некорретная дата")
                false
            }
            !isValidDate(set_date_issue.getText()) -> {
                set_date_issue.showError("Некорретная дата")
                false
            }
            !isValidDate(set_date_expiry.getText()) -> {
                set_date_expiry.showError("Некорретная дата")
                false
            }
            set_authority.length() < Constant.Length.NAME -> {
                set_authority.showError("Некорретный орган выдачи")
                false
            }
            set_address.isVisible && set_address.length() < Constant.Length.NAME -> {
                set_address.showError("Некорретный адрес")
                false
            }

            else -> true
        }
    }

    private fun isValidDate(stringDate: String) = stringDate.filter { it.isDigit() }.length == 8


    private fun onNextClick() = AlertDialog.Builder(this)
        .setMessage(R.string.confirm_recognition_query)
        .setPositiveButton(R.string.confirm) { _, _ -> confirmInfo() }
        .setNegativeButton(R.string.cancel, null)
        .create()
        .show()


    private fun confirmInfo() {
        val info = RecognitionInfo(
            set_inn.getText(), set_passport.getText(), set_name.getText(),
            set_surname.getText(), set_patronymic.getText(), set_date_birth.getText(),
            set_date_expiry.getText(), set_date_issue.getText(), set_authority.getText(),
            userDidNotChangeData
        )
        viewModel.confirmRecognitionInfo(info)
    }

    private fun subscribeToLiveData() {
        viewModel.recognitionInfo.observe(this, Observer {
            it?.let { setInfo(it) }
        })

        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is Event.Success -> {
                        successHandler()
                    }
                }
            }
        })
    }

    private fun successHandler() {
        if (isForModeration()) {
            setResult(Activity.RESULT_OK); finish()
        } else {
            RegistrationConfirmationActivity.start(this, user.sessionId ?: "")
        }
    }

    private fun isForModeration() = intent.getBooleanExtra(Constant.Extra.FOR_MODERATION, false)

    private fun setInfo(it: RecognitionInfo) {
        it.run {
            set_passport.getEditText().setText(passportNumber)
            set_inn.getEditText().setText(inn)
            set_surname.getEditText().setText(surname)
            set_name.getEditText().setText(name)
            set_patronymic.getEditText().setText(patronymic)
            set_date_birth.getEditText().setText(dateBirth)
            set_date_expiry.getEditText().setText(dateExpiry)
            set_date_issue.getEditText().setText(dateIssue)
            set_authority.getEditText().setText(authority)
        }
    }

    companion object {
        fun start(activity: SimpleActivity) {
            activity.startActivity(activity.createIntent(PassportInfoActivity::class))
        }

        fun startForResult(activity: BaseActivity<*>) {
            activity.startActivityForResult(
                activity.createIntent(PassportInfoActivity::class)
                    .putExtra(Constant.Extra.FOR_MODERATION, true), 0
            )
        }
    }
}
