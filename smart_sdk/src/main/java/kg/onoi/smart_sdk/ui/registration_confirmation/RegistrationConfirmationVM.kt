package kg.onoi.smart_sdk.ui.registration_confirmation

import androidx.lifecycle.MutableLiveData
import kg.onoi.smart_sdk.models.ConfirmationDocument
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class RegistrationConfirmationVM(private val registrationRepo: RegistrationRepo) : BaseViewModel() {

    val documents: MutableLiveData<List<ConfirmationDocument>> = MutableLiveData()
    val title: MutableLiveData<String> = MutableLiveData()

    fun fetchCheckBoxes(sessionId: String) {
        runWithProgress {
            val response = registrationRepo.fetchCompleteDocuments(sessionId)
            documents.value = response.documents
            title.value = response.title
        }
    }

    fun setupTitle(text: String) {
        title.value = text
    }
}