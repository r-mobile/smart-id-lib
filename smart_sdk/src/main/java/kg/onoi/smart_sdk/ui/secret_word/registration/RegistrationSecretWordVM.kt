package kg.onoi.smart_sdk.ui.secret_word.registration

import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class RegistrationSecretWordVM(private val repo: RegistrationRepo) : BaseViewModel() {
    fun saveSecretWord(sessionId: String, secretWord: String) {
        runWithProgress {
            event.value = when (repo.saveSecretWord(sessionId, secretWord)) {
                true -> Event.Success()
                else -> Event.Fail("")
            }
        }
    }
}