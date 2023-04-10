package kg.onoi.smart_sdk.ui.auth_by_inn

import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import kg.onoi.smart_sdk.ui.pin.EmployeePincodeActivity
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.getString
import kg.onoi.smart_sdk.extensions.gone
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.extensions.showToast
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.InnAuthEvent
import kg.onoi.smart_sdk.utils.ExtrasName
import kg.onoi.smart_sdk.utils.SdkConfig
import kotlinx.android.synthetic.main.activity_sign_in.*

class EmployeeInnActivity : BaseInnActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeLiveData()
    }

    override fun initViews() {
        super.initViews()
        tv_individual_entity.gone()
        set_inn.setTitle(R.string.input_employee_pin)
        btn_next.setOnClickListener { viewModel.checkEmployeeInn(getOrgInn(), set_inn.getText()) }
    }

    private fun getOrgInn(): String = intent.getStringExtra(ExtrasName.ORG_INN) ?: ""

    private fun subscribeLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is InnAuthEvent.SuccessFetchedName -> setName(it.name)
                    is InnAuthEvent.MissingFetchedName -> setInvalidState("Пользователь с ИНН ${set_inn.getText()} не зарегистрирован.")
                    is InnAuthEvent.InputPinEvent -> EmployeePincodeActivity.start(
                        this,
                        getOrgInn(),
                        it.inn,
                        it.isPermanent
                    )
                    is Event.UnknownErrorEvent -> showToast(getString(R.string.unknow_exception))
                    is Event.ErrorEvent -> showSimpleAlert(it.message)
                    is InnAuthEvent.UserNotFound -> {
                        val message = "Пользователь с ИНН ${set_inn.getText()} не зарегистрирован."
                        if(SdkConfig.enableRegistration) showRegistrationQuery("$message Зарегистрироваться?")
                        else showToast(message)
                    }
                    else -> {
                    }
                }
            }
        })
    }

    companion object {
        fun start(activity: Activity, orgInn: String) {
            val intent = Intent(activity, EmployeeInnActivity::class.java)
            intent.putExtra(ExtrasName.ORG_INN, orgInn)
            activity.startActivityForResult(intent, 0)
        }
    }
}