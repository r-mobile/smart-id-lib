package kg.onoi.smart_sdk.ui.passport_info

import androidx.lifecycle.MutableLiveData
import kg.onoi.smart_sdk.models.User
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.RecognitionInfo
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class PassportInfoVM(private val repo: RegistrationRepo) : BaseViewModel() {

    var recognitionInfo = MutableLiveData<RecognitionInfo>()
    lateinit var user: User

    fun setupUser(user: User) {
        this.user = user
    }

    internal fun updateInfoFromRecognition() {
        recognitionInfo.value = RecognitionInfo(
            user.pin ?: "", user.passportNumber ?: "",
            user.name ?: "", user.surname ?: "", user.patronymic ?: "",
            user.dateBirth ?: "", user.dateExpiry ?: "", user.dateIssue ?: "",
            user.authority ?: ""
        )
    }

    fun confirmRecognitionInfo(info: RecognitionInfo) {
        runWithProgress {
            repo.confirmUserRegistrationInfoAsync(user.sessionId, info)
            user.isCompleted = true
            updateUserOnComplete(info)
            event.value = Event.Success()
        }
    }

    private fun updateUserOnComplete(info: RecognitionInfo) {
        user.apply {
            isCompleted = true
            name = info.name
            surname = info.surname
            patronymic = info.patronymic
            pin = info.inn
            passportNumber = info.passportNumber
        }
    }
}