package kg.onoi.smart_sdk.repositories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import kg.onoi.smart_sdk.ui.close.CloseSessionVM
import kg.onoi.smart_sdk.ui.face_detection.vm.AuthFaceDetectionVM
import kg.onoi.smart_sdk.ui.auth_by_inn.InnInputVM
import kg.onoi.smart_sdk.ui.auth_by_phone.AuthVM
import kg.onoi.smart_sdk.ui.moderation.ModerationVM
import kg.onoi.smart_sdk.ui.passport_info.PassportInfoVM
import kg.onoi.smart_sdk.ui.registration_by_phone.RegistrationVM
import kg.onoi.smart_sdk.ui.photos.PhotoActivityVM
import kg.onoi.smart_sdk.ui.pin.PincodeVM
import kg.onoi.smart_sdk.ui.registration_complete.RegistrationStatusVM
import kg.onoi.smart_sdk.ui.registration_confirmation.RegistrationConfirmationVM
import kg.onoi.smart_sdk.ui.secret_word.auth.AuthSecretWordVM
import kg.onoi.smart_sdk.ui.secret_word.registration.RegistrationSecretWordVM
import kg.onoi.smart_sdk.ui.video_moderation.VideoModerationVM

class ViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass.name) {
            RegistrationVM::class.java.name -> RegistrationVM(RegistrationRepo(context))
            PhotoActivityVM::class.java.name -> PhotoActivityVM(
                RegistrationRepo(context),
                AuthRepo()
            )
            RegistrationStatusVM::class.java.name -> RegistrationStatusVM(RegistrationRepo(context))
            ModerationVM::class.java.name -> ModerationVM(RegistrationRepo(context))
            InnInputVM::class.java.name -> InnInputVM()
            PincodeVM::class.java.name -> PincodeVM()
            CloseSessionVM::class.java.name -> CloseSessionVM()
            AuthFaceDetectionVM::class.java.name -> AuthFaceDetectionVM(AuthRepo())
            AuthVM::class.java.name -> AuthVM(AuthRepo())
            RegistrationSecretWordVM::class.java.name -> RegistrationSecretWordVM(
                RegistrationRepo(context)
            )
            AuthSecretWordVM::class.java.name -> AuthSecretWordVM(AuthRepo())
            RegistrationConfirmationVM::class.java.name -> RegistrationConfirmationVM(RegistrationRepo(context))
            VideoModerationVM::class.java.name -> VideoModerationVM(RegistrationRepo(context))
            else -> PassportInfoVM(RegistrationRepo(context))
        } as T
    }

}