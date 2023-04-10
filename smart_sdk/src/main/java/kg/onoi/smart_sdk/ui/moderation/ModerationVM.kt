package kg.onoi.smart_sdk.ui.moderation

import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kg.onoi.smart_sdk.utils.SdkHelper

class ModerationVM(private val repo: RegistrationRepo) : BaseViewModel() {

    var moderationResult: Moderation? = null
    var sessionId: String? = SdkHelper.sessionId

    fun resubmit() {
        runWithProgress {
            repo.resubmitRegistrationAsync(sessionId ?: "")
            event.value = Event.Success()
        }
    }

    fun cancelRegistration() {
        runWithProgress {
            repo.cancelRegistration(sessionId)
            event.value = ModerationEvents.CancelRegistration()
        }
    }

}