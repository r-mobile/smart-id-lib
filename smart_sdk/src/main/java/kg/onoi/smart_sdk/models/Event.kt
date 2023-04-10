package kg.onoi.smart_sdk.models

sealed class Event {
    class Success : Event()
    class Fail(val message: String) : Event()
    class Message(val message: String) : Event()
    class ErrorEvent(val message: String) : Event()
    class UnknownErrorEvent : Event()
}

sealed class PhoneRegistrationEvents : Event() {
    class SuccessSmsSend(val sessionId: String) : PhoneRegistrationEvents()
}

sealed class RegistrationCompleteEvents : Event() {
    class SuccessComplete : RegistrationCompleteEvents()
    class RegistrationNotFound : RegistrationCompleteEvents()
    class InternalError : RegistrationCompleteEvents()
    class RegistrationRefused(val result: Moderation) : RegistrationCompleteEvents()
    class WaitingComplete : RegistrationCompleteEvents()
    class WaitingVideoModeration : RegistrationCompleteEvents()
}

sealed class ModerationEvents : Event() {
    class CancelRegistration : ModerationEvents()
}

sealed class InnAuthEvent : Event() {
    class SuccessFetchedName(val name: String) : InnAuthEvent()
    class MissingFetchedName : InnAuthEvent()
    class InputEmployeeInnEvent(val orgInn: String) : InnAuthEvent()
    class InputPinEvent(val inn: String, val isPermanent: Boolean) : InnAuthEvent()
    class UserNotFound : InnAuthEvent()
}

sealed class PincodeEvent : Event() {
    class SuccessSessionResponseEvent(val sessionId: String) : PincodeEvent()
    class InvalidSessionResponseEvent : PincodeEvent()
    class NonApprovedResponseEvent : PincodeEvent()
    class SmsRepeateSuccessEvent : PincodeEvent()
    class RequestFaceRecognitionEvent(val sessionId: String) : PincodeEvent()
    class RepeateTimeoutEvent(val timeout: Int) : PincodeEvent()
}

sealed class NextStepEvents : Event() {
    class ShowPassportInfoEvent: NextStepEvents()
    class ShowSecretWordEvent: NextStepEvents()
}

class SuccessSessionCloseEvent : Event()

sealed class AuthByPhoneEvents : Event() {
    class SuccessSmsSend(val phone: String) : AuthByPhoneEvents()
    class UserNotFound : AuthByPhoneEvents()
    class NonApprovedResponseEvent : AuthByPhoneEvents()
    class InvalidSessionResponseEvent : AuthByPhoneEvents()
    class RequestFaceRecognitionEvent(val sessionId: String, val needSecretWord: Boolean) :
        AuthByPhoneEvents()
    class SuccessSessionResponseEvent(val sessionId: String) : AuthByPhoneEvents()
}


sealed class VideoModerationEvent: Event() {
    class UpdateModerationScheduleInfo(val info: ModerationSchedule): VideoModerationEvent()
    class VideoModerationLinkFetched(val info: VideoModerationResponse): VideoModerationEvent()
    class VideoModerationStatusFetched(val status: VideoModerationStatus): VideoModerationEvent()
}
