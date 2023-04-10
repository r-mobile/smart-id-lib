package kg.onoi.smart_sdk.repositories

import android.content.Context
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.network.RegisterApi
import kg.onoi.smart_sdk.network.RetrofitWrapper
import kg.onoi.smart_sdk.utils.SdkConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RegistrationRepo(val context: Context) {

    private val api: RegisterApi by lazy {
        RetrofitWrapper(SdkConfig.registrationHost, RetrofitWrapper.Type.REGISTRATION)
            .getApi(RegisterApi::class.java)
    }

    suspend fun startRegistrationAsync(phone: String?): PhoneRegisterResponse {
        return api.startRegistration(phone?.replace("+", ""))
    }

    suspend fun checkSmsCodeAsync(sessionId: String?, smsCode: String): Boolean {
        return api.checkSmsCode(sessionId, smsCode)
    }

    suspend fun cancelRegistration(sessionId: String?) = api.cancelRegistration(sessionId)

    suspend fun fetchRegistrationStatusAsync(sessionId: String?): Moderation {
        return api.fetchRegistrationStatus(sessionId)
    }

    suspend fun uploadPassportFrontAsync(
        path: String,
        docType: DocumentType,
        sessionId: String?
    ): Boolean? {
        return api.uploadPassportFront(sessionId, docType.type, fileAsMultipart(path))
    }

    suspend fun uploadPassportBackAsync(
        path: String,
        docType: DocumentType,
        sessionId: String?
    ): Boolean? {
        return api.uploadPassportBack(sessionId, docType.type, fileAsMultipart(path))
    }

    suspend fun uploadUserPhotoWithPassportAsync(path: String, sessionId: String?): Boolean? {
        return api.uploadUserPhotoWithPassport(sessionId, fileAsMultipart(path))
    }

    suspend fun uploadUserPhotoAsync(path: String, sessionId: String?): Boolean? {
        return api.uploadUserPhoto(sessionId, fileAsMultipart(path))
    }

    suspend fun confirmUserRegistrationInfoAsync(
        sessionId: String?,
        recognitionInfo: RecognitionInfo
    ) {
        return api.сonfirmUserRegistrationInfo(sessionId, recognitionInfo)
    }

    suspend fun resubmitRegistrationAsync(sessionId: String?) {
        return api.resubmitRegistration(sessionId)
    }

    private fun fileAsMultipart(filename: String): MultipartBody.Part {
        val reqFile = File(filename).asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", filename, reqFile)
    }

    suspend fun saveSecretWord(sessionId: String, secretWord: String): Boolean {
        return api.saveSecretWord(sessionId, SetSecretWord(secretWord))
    }

    suspend fun fetchCompleteDocuments(sessionId: String): ConfirmationDocumentResponse {
        return api.fetchCompleteDocuments(sessionId)
    }

    suspend fun fetchNextStep(sessionId: String?): RegistrationSteps {
        return api.fetchNextStep(sessionId)
    }

    suspend fun fetchSettings(sessionId: String?): RegistrationSettings {
        return api.fetchSettings(sessionId)
    }

    suspend fun getVideoModerationSchedule(sessionId: String?): ModerationSchedule {
        return api.getVideoModerationSchedule(sessionId)
    }

    suspend fun getVideoModerationResponse(sessionId: String?): VideoModerationResponse {
        return api.getVideoModerationResponse(sessionId)
    }

    suspend fun getVideoModerationStatus(sessionId: String?, requestId: Int): VideoModerationStatus {
        return api.getVideoModerationStatus(sessionId, requestId)
    }

}