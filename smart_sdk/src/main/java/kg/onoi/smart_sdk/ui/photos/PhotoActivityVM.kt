package kg.onoi.smart_sdk.ui.photos

import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.getOrNull
import kg.onoi.smart_sdk.extensions.toFirstCapitalize
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.recognition.ResultRecognitionWrapper
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhotoActivityVM(private val repo: RegistrationRepo, private val authRepo: AuthRepo) :
    BaseViewModel() {

    lateinit var user: User
    lateinit var photoType: PhotoType


    fun getPrePhotoDescription(): Int = when (photoType) {
        PhotoType.PASSPORT_FRONT -> R.string.create_passport_front_photo
        PhotoType.PASSPORT_BACK -> R.string.create_passport_back_photo
        PhotoType.SELFIE_W_PASSPORT -> R.string.create_passport_selfie_photo
        PhotoType.SELFIE -> R.string.create_selfie_photo
    }

    fun getPostPhotoDescription(): Int = when (photoType) {
        PhotoType.PASSPORT_FRONT -> R.string.passport_front_side
        PhotoType.PASSPORT_BACK -> R.string.passport_back_side
        PhotoType.SELFIE_W_PASSPORT -> R.string.create_passport_selfie_photo
        PhotoType.SELFIE -> R.string.create_selfie_photo
    }

    fun setTargetPhotoType(type: PhotoType) {
        photoType = type
    }

    fun getDocType(): DocumentType = user.documentType ?: DocumentType.PASSPORT_OLD

    fun getSelfieWithPassportPlaceholder(): Int {
        return R.drawable.selfie_with_passport
    }

    fun getSelfiePlaceholder(): Int {
        return R.drawable.selfie_with_passport
    }

    fun uploadPhoto(path: String) {
        when (photoType) {
            PhotoType.PASSPORT_FRONT -> uploadFrontType(path, getDocType())
            PhotoType.PASSPORT_BACK -> uploadBackType(path, getDocType())
            PhotoType.SELFIE_W_PASSPORT -> uploadSelfieWithPassport(path)
            PhotoType.SELFIE -> uploadSelfie(path)
        }
    }

    private fun uploadFrontType(path: String, documentType: DocumentType) {
        runWithProgress {
            val isSuccess =
                repo.uploadPassportFrontAsync(path, documentType, user.sessionId)
            if (isSuccess == true) {
                event.value = Event.Success()
            }
        }
    }

    private suspend fun updateUserFields() = withContext(Dispatchers.IO) {
        user.apply {
            //TODO Fix it later
            val recognitionMap = mutableMapOf<String, String>()

            frontRecognitionInfo?.recognitionFields?.toMap()?.let {
                recognitionMap.putAll(it)
            }

            backRecognitionInfo?.recognitionFields?.toMap()?.let {
                recognitionMap.putAll(it.minus(recognitionMap.keys))
            }

            name = recognitionMap.getOrNull("name")?.toFirstCapitalize()
            patronymic = recognitionMap.getOrNull("patronymic")?.toFirstCapitalize()
            surname = recognitionMap.getOrNull("surname")?.toFirstCapitalize()
            passportNumber = recognitionMap.getOrNull("number")
            pin = recognitionMap.getOrNull("id_number")
            dateBirth = recognitionMap.getOrNull("birth_date")
            dateExpiry = recognitionMap.getOrNull("expiry_date")
            dateIssue = recognitionMap.getOrNull("issue_date")
            authority = recognitionMap.getOrNull("authority")

            frontRecognitionInfo = null
            backRecognitionInfo = null
        }
    }

    private fun uploadBackType(path: String, documentType: DocumentType) {
        runWithProgress {
            val isSuccess = repo.uploadPassportBackAsync(path, documentType, user.sessionId)
            if (isSuccess == true) {
                updateUserFields()
                event.value = Event.Success()
            }
        }
    }

    private fun uploadSelfieWithPassport(path: String) {
        runWithProgress {
            Utils.compressAndResize(path)
            val isSuccess = repo.uploadUserPhotoWithPassportAsync(path, user.sessionId)
            if (isSuccess == true) {
                event.value = Event.Success()
            }
        }
    }

    private fun uploadSelfie(path: String) {
        runWithProgress {
            event.value = try {
                Utils.compressAndResize(path)
                val isSuccess = repo.uploadUserPhotoAsync(path, user.sessionId)
                if (isSuccess == true) {
                    Event.Success()
                } else {
                    Event.Fail("")
                }
            } catch (e: Exception) {
                Event.Fail(e.message ?: "")
            }

        }
    }

    fun saveRecognitionResult(result: ResultRecognitionWrapper?) {
        result?.let {
            user.documentType = it.getTypeAsDocumentType()
            when (photoType) {
                PhotoType.PASSPORT_FRONT -> {
                    user.frontRecognitionInfo = it
                }
                PhotoType.PASSPORT_BACK -> {
                    user.backRecognitionInfo = it
                }
            }
        }
    }

    fun checkIsOneDocument(result: ResultRecognitionWrapper?): Boolean = when {
        user.frontRecognitionInfo == null -> true
        else -> user.frontRecognitionInfo?.isDataOfOneDocument(result) ?: false
    }

    fun resetRecognitionInfo() {
        when (photoType) {
            PhotoType.PASSPORT_FRONT -> {
                user.frontRecognitionInfo = null
            }
            PhotoType.PASSPORT_BACK -> {
                user.backRecognitionInfo = null
            }
        }
    }

}