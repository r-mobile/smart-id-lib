package kg.onoi.smart_sdk.ui.auth_by_inn

import androidx.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import kg.onoi.smart_sdk.ui.pin.MainPincodeActivity
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.getString
import kg.onoi.smart_sdk.extensions.showToast
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.InnAuthEvent
import kg.onoi.smart_sdk.utils.SdkConfig
import kg.onoi.smart_sdk.utils.SdkHelper
import kg.onoi.smart_sdk.utils.SignType
import kotlinx.android.synthetic.main.activity_sign_in.*

class MainInnActivity : BaseInnActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeLiveData()
    }

    override fun initViews() {
        super.initViews()
        tv_individual_entity.isVisible = SdkHelper.signType == SignType.COMMON
        set_inn.setTitle(R.string.input_organization_pin)
        btn_next.setOnClickListener { viewModel.checkInn(set_inn.getText()) }
    }

    private fun subscribeLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is InnAuthEvent.SuccessFetchedName -> setName(it.name)
                    is InnAuthEvent.MissingFetchedName -> setInvalidState("Организация с ИНН ${set_inn.getText()} не зарегистрирована.")
                    is InnAuthEvent.InputEmployeeInnEvent -> EmployeeInnActivity.start(
                        this,
                        it.orgInn
                    )
                    is InnAuthEvent.InputPinEvent -> MainPincodeActivity.start(this, it.inn,  it.isPermanent)
                    is Event.UnknownErrorEvent -> showToast(getString(R.string.unknow_exception))
                    is Event.ErrorEvent -> showToast(it.message)
                    is InnAuthEvent.UserNotFound -> {
                        val message = "Организация с ИНН ${set_inn.getText()} не зарегистрирована."
                        if(SdkConfig.enableRegistration) showRegistrationQuery("$message Зарегистрироваться?")
                        else showToast(message)
                    }
                }
            }
        })
    }
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainInnActivity::class.java))
        }
    }
}