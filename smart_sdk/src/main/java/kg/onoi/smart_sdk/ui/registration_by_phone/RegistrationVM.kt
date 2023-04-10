package kg.onoi.smart_sdk.ui.registration_by_phone

import kg.onoi.smart_sdk.extensions.removeWhitespaces
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.PhoneRegistrationEvents
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kg.onoi.smart_sdk.utils.SdkHelper

class RegistrationVM(private val repo: RegistrationRepo) : BaseViewModel() {

    fun sendConfirmSms(phone: String?) {
        runWithProgress {
            val response = repo.startRegistrationAsync(phone?.removeWhitespaces())
            event.value = if (response.phoneAlreadyRegistered) {
                Event.Fail(response.message ?: "")
            } else {
                PhoneRegistrationEvents.SuccessSmsSend(response.sessionId)
            }
        }
    }

    fun checkSmsCode(sessionId: String?, smsCode: String) {
        runWithProgress {
            val response = repo.checkSmsCodeAsync(sessionId, smsCode)
            if(response) SdkHelper.settings = repo.fetchSettings(sessionId)
            event.value = when (response) {
                true -> Event.Success()
                else -> Event.Fail("")
            }
        }
    }
}