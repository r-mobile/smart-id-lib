package kg.onoi.smart_sdk.models

data class PhoneRegisterResponse(
    var phoneAlreadyRegistered: Boolean,
    var sessionId: String,
    var name: String,
    var message: String?
)