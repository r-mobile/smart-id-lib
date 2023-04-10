package kg.onoi.smart_sdk.models

import com.google.gson.annotations.SerializedName

data class DefaultResponse(val success: Boolean, val code: Int, val message: String)

data class Error(val code: Int, val message: String)

data class Inn(val inn: String)

data class Name(val name: String)

data class CheckAccessToSendSms(val timeToNextSendSms: Int, val countSendPinAttempt: Int)

data class InnCheck(
    val inn: String,
    val fullName: String,
    val success: Boolean,
    val next: NextStepType,
    val isPinPermanent: Boolean
)

data class EmployeeInnCheck(
    val innOrganisation: String,
    val innEmployee: String,
    val success: Boolean,
    val next: NextStepType,
    val isPinPermanent: Boolean
)

data class Employee(val innOrganisation: String? = null, val innEmployee: String? = null)

data class PinInfo(val pin: String, val inn: String, val app: String)

data class EmployeePinInfo(
    val pin: String,
    val innOrganisation: String?,
    val innEmployee: String?,
    val app: String
)

data class PinConfirmResult(
    val approve: Boolean,
    val inn: String?,
    val sessionId: String?,
    val fullName: String?,
    val faceRecognition: Boolean,
    val needSecretWord: Boolean
)

data class EmployeePinConfirmResult(
    val approve: Boolean, val innOrganisation: String?, val innEmployee: String?,
    val sessionId: String?, val faceRecognition: Boolean
)

enum class NextStepType {
    @SerializedName("give-me-pin")
    GiveMePin,

    @SerializedName("give-me-personal-inn")
    GiveMeInn
}

class AuthByPhoneResult(val message: String?, val result: UserFoundType)

enum class UserFoundType {
    @SerializedName("userFound")
    USERFOUND,

    @SerializedName("userNotFound")
    USERNOTFOUND
}

data class AuthSmsConfirmation(val phone: String, val pin: String, val app: String)