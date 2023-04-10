package kg.onoi.smart_sdk.ui.pin

import kg.onoi.smart_sdk.models.EmployeePinConfirmResult
import kg.onoi.smart_sdk.models.PinConfirmResult
import kg.onoi.smart_sdk.models.PincodeEvent
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class PincodeVM : BaseViewModel() {

    private val authRepo: AuthRepo by lazy { AuthRepo() }

    fun pinConfirm(pin: String, inn: String) {
        runWithProgress {
            handlePinConfirmation(authRepo.pinConfirm(pin, inn))
        }
    }

    private fun handlePinConfirmation(pinResult: PinConfirmResult?) {
        event.value = when {
            pinResult?.approve == false -> PincodeEvent.NonApprovedResponseEvent()
            pinResult?.sessionId.isNullOrEmpty() -> PincodeEvent.InvalidSessionResponseEvent()
            pinResult?.faceRecognition == true -> PincodeEvent.RequestFaceRecognitionEvent(pinResult.sessionId!!)
            else -> PincodeEvent.SuccessSessionResponseEvent(pinResult!!.sessionId!!)
        }
    }

    fun employeePinConfirm(pin: String, orgInn: String, inn: String) {
        runWithProgress {
            handleEmployeePinConfirmation(authRepo.employeePinConfirm(pin, orgInn, inn))
        }
    }

    private fun handleEmployeePinConfirmation(pinResult: EmployeePinConfirmResult?) {
        event.value = when {
            pinResult?.approve == false -> PincodeEvent.NonApprovedResponseEvent()
            pinResult?.sessionId.isNullOrEmpty() -> PincodeEvent.InvalidSessionResponseEvent()
            pinResult?.faceRecognition == true -> PincodeEvent.RequestFaceRecognitionEvent(pinResult.sessionId!!)
            else -> PincodeEvent.SuccessSessionResponseEvent(pinResult!!.sessionId!!)
        }
    }

    fun repeatRequest(inn: String) {
        runWithProgress {
            handleRepeatResult(authRepo.checkInn(inn)?.success, inn)
        }
    }

    fun repeatRequest(orgInn: String, inn: String) {
        runWithProgress {
            handleRepeatResult(authRepo.checkEmployeeInn(orgInn, inn)?.success, inn, orgInn)
        }
    }

    private fun handleRepeatResult(isSuccess: Boolean?, inn: String, orgInn: String? = null) {
        if (isSuccess == true) {
            repeatRequestTimeout(inn, orgInn)
            event.value = PincodeEvent.SmsRepeateSuccessEvent()
        }
    }

    fun repeatRequestTimeout(employeeInn: String, orgInn: String? = null) {
        runWithProgress {
            val timeToNextSendSms =
                authRepo.checkAccessToSendSmsAndPin(employeeInn, orgInn).timeToNextSendSms
            event.value = PincodeEvent.RepeateTimeoutEvent(timeToNextSendSms)
        }
    }
}