package kg.onoi.smart_sdk.ui.close

import kg.onoi.smart_sdk.models.SuccessSessionCloseEvent
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class CloseSessionVM : BaseViewModel() {
    private val authRepo: AuthRepo by lazy { AuthRepo() }

    fun closeSession(sessionId: String) {
        runWithProgress {
            authRepo.closeSession(sessionId)
            event.value = SuccessSessionCloseEvent()
        }
    }
}