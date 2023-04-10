package kg.onoi.smart_sdk.ui.face_detection.vm

import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class AuthFaceDetectionVM(private val authRepo: AuthRepo) : BaseViewModel() {

    fun uploadAuthPhoto(absolutePath: String) {
        runWithProgress {
            val result = authRepo.uploadUserPhoto(absolutePath)
            event.value = if (result.success) Event.Success() else Event.ErrorEvent(result.message)
        }
    }
}