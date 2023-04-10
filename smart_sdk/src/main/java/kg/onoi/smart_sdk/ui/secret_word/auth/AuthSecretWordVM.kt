package kg.onoi.smart_sdk.ui.secret_word.auth

import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class AuthSecretWordVM(private val repo: AuthRepo) : BaseViewModel() {
    fun checkSecretWord(sessionId: String, secretWord: String) {
        runWithProgress {
            event.value = when (repo.checkSecretWord(sessionId, secretWord).success) {
                true -> Event.Success()
                else -> Event.Fail("")
            }
        }
    }
}