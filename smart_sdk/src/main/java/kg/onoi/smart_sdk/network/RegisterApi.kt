package kg.onoi.smart_sdk.network

import kg.onoi.smart_sdk.models.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface RegisterApi {

    @POST("Registration/StartRegistration/{phone}")
    suspend fun startRegistration(@Path("phone") phone: String?): PhoneRegisterResponse

    @POST("Registration/CheckSmsCode/{sessionId}/{smsCode}")
    suspend fun checkSmsCode(
        @Path("sessionId") sessionId: String?,
        @Path("smsCode") smsCode: String
    ): Boolean

    @POST("Registration/UploadPassportFront/{sessionId}/{type}")
    @Multipart
    suspend fun uploadPassportFront(
        @Path("sessionId") sessionId: String?,
        @Path("type") type: String,
        @Part image: MultipartBody.Part
    ): Boolean?

    @POST("Registration/UploadPassportBack/{sessionId}/{type}")
    @Multipart
    suspend fun uploadPassportBack(
        @Path("sessionId") sessionId: String?,
        @Path("type") type: String,
        @Part image: MultipartBody.Part
    ): Boolean?

    @POST("Registration/UploadUserPhoto/{sessionId}")
    @Multipart
    suspend fun uploadUserPhoto(
        @Path("sessionId") sessionId: String?,
        @Part image: MultipartBody.Part
    ): Boolean?

    @POST("Registration/UploadUserPhotoWithPassport/{sessionId}")
    @Multipart
    suspend fun uploadUserPhotoWithPassport(
        @Path("sessionId") sessionId: String?,
        @Part image: MultipartBody.Part
    ): Boolean?

    @POST("Registration/ConfirmUserRegistrationInfo/{sessionId}")
    suspend fun сonfirmUserRegistrationInfo(
        @Path("sessionId") sessionId: String?,
        @Body recognitionInfo: RecognitionInfo
    ): Unit

    @GET("Registration/GetRegistrationStatus/{sessionId}")
    suspend fun fetchRegistrationStatus(@Path("sessionId") sessionId: String?): Moderation

    @POST("Registration/Resubmit/{sessionId}")
    suspend fun resubmitRegistration(@Path("sessionId") sessionId: String?)

    @POST("Registration/CancelRegistration/{sessionId}")
    suspend fun cancelRegistration(@Path("sessionId") sessionId: String?)

    @POST("Registration/SetSecretWord/{sessionId}")
    suspend fun saveSecretWord(
        @Path("sessionId") sessionId: String?,
        @Body secretWord: SetSecretWord
    ): Boolean

    @GET("Documents/GetDocumentsForCompletingRegistration/{sessionId}")
    suspend fun fetchCompleteDocuments(@Path("sessionId") sessionId: String?): ConfirmationDocumentResponse

    @GET("Registration/GetNextStep/{sessionId}")
    suspend fun fetchNextStep(@Path("sessionId") sessionId: String?): RegistrationSteps

    @GET("Registration/GetSettings/{sessionId}")
    suspend fun fetchSettings(@Path("sessionId") sessionId: String?): RegistrationSettings

    @GET("Registration/GetVideoModerationSchedule/{sessionId}")
    suspend fun getVideoModerationSchedule(@Path("sessionId") sessionId: String?) : ModerationSchedule

    @POST("Registration/StartVideoModeration/{sessionId}")
    suspend fun getVideoModerationResponse(@Path("sessionId") sessionId: String?) : VideoModerationResponse

    @GET("/api/Registration/GetVideoModerationStatus/{sessionId}/{requestId}")
    suspend fun getVideoModerationStatus(@Path("sessionId") sessionId: String?, @Path("requestId") requestId: Int): VideoModerationStatus
}