package kg.onoi.smart_sdk.network

import kg.onoi.smart_sdk.models.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface AuthApi {

    @POST("Auth/GetNameByInn")
    suspend fun fetchNameByInn(@Body inn: Inn): Name?

    @POST("Auth/AuthCheck")
    suspend fun checkInn(@Body inn: Inn): InnCheck

    @POST("Auth/EmployeeCheck")
    suspend fun checkEmployeeInn(@Body inn: Employee): EmployeeInnCheck

    @POST("Auth/PinConfirm")
    suspend fun pinConfirm(@Body pinInfo: PinInfo): PinConfirmResult

    @POST("Auth/EmployeePinConfirm")
    suspend fun employeePinConfirm(@Body inn: EmployeePinInfo): EmployeePinConfirmResult

    @POST("Session/CloseSession/{sessionId}")
    suspend fun closeSession(@Path("sessionId") sessionId: String)

    @POST("Document/UploadUserPhotoForVerification/{sessionId}")
    @Multipart
    suspend fun uploadUserPhoto(@Path("sessionId") sessionId: String, @Part image: MultipartBody.Part): DefaultResponse

    @GET("auth/CheckAccessToSendSmsAndPin/{inn}")
    suspend fun checkAccessToSendSmsAndPin(@Path("inn") employeeInn: String): CheckAccessToSendSms

    @GET("auth/CheckAccessToSendSmsAndPin/{inn}")
    suspend fun checkAccessToSendSmsAndPin(@Path("inn") employeeInn: String, @Query("organizationInn") organizationInn: String): CheckAccessToSendSms

    @POST("auth/AuthByPhone/{phone}")
    suspend fun authByPhone(@Path("phone") phone: String?): AuthByPhoneResult

    @POST("auth/PhonePinConfirm")
    suspend fun checkSmsCode(@Body authSmsConfirmation: AuthSmsConfirmation): PinConfirmResult

    @POST("auth/SetSecretWord/{sessionId}")
    suspend fun checkSecretWord(
        @Path("sessionId") sessionId: String?,
        @Body secretWord: SetSecretWord
    ): DefaultResponse
}