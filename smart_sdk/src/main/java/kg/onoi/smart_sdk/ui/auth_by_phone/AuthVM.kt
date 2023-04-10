package kg.onoi.smart_sdk.ui.auth_by_phone

import kg.onoi.smart_sdk.extensions.removeWhitespaces
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kg.onoi.smart_sdk.utils.SdkConfig
import java.security.AuthProvider

class AuthVM(private val repo: AuthRepo) : BaseViewModel() {

    fun sendConfirmSms(phone: String) {
        val formattedNumber = phone?.removeWhitespaces()?.removeRange(0, 1) ?: ""
        runWithProgress {
            val response = repo.authByPhone(formattedNumber)
            event.value = when (response?.result) {
                UserFoundType.USERFOUND -> AuthByPhoneEvents.SuccessSmsSend(formattedNumber)
                else -> AuthByPhoneEvents.UserNotFound()
            }
        }
    }

    fun checkSmsCode(phone: String, pin: String) {
        runWithProgress {
            handleSmsConfirmation(repo.checkSmsCode(phone, pin, SdkConfig.appName))
        }
    }

    private fun handleSmsConfirmation(pinResult: PinConfirmResult?) {
        event.value = when {
            pinResult?.approve == false -> AuthByPhoneEvents.NonApprovedResponseEvent()
            pinResult?.sessionId.isNullOrEmpty() -> AuthByPhoneEvents.InvalidSessionResponseEvent()
            pinResult?.faceRecognition == true -> AuthByPhoneEvents.RequestFaceRecognitionEvent(pinResult.sessionId!!, pinResult.needSecretWord)
            else -> AuthByPhoneEvents.SuccessSessionResponseEvent(pinResult!!.sessionId!!)
        }
    }
}