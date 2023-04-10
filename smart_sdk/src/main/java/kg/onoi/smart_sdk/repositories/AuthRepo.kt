package kg.onoi.smart_sdk.repositories

import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.network.AuthApi
import kg.onoi.smart_sdk.network.RetrofitWrapper
import kg.onoi.smart_sdk.utils.SdkConfig
import kg.onoi.smart_sdk.utils.SdkHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AuthRepo {

    private val authApi: AuthApi by lazy {
        RetrofitWrapper(SdkConfig.authHost, RetrofitWrapper.Type.AUTHORIZATION)
            .getApi(AuthApi::class.java)
    }

    suspend fun fetchNameByInn(inn: String): Name? {
        return authApi.fetchNameByInn(Inn(inn))
    }

    suspend fun checkInn(inn: String): InnCheck? {
        return authApi.checkInn(Inn(inn))
    }

    suspend fun checkEmployeeInn(orgInn: String, employeeInn: String): EmployeeInnCheck? {
        return authApi.checkEmployeeInn(Employee(orgInn, employeeInn))
    }

    suspend fun checkAccessToSendSmsAndPin(
        employeeInn: String,
        orgInn: String? = null
    ): CheckAccessToSendSms {
        return when (orgInn) {
            null -> authApi.checkAccessToSendSmsAndPin(employeeInn)
            else -> authApi.checkAccessToSendSmsAndPin(employeeInn, orgInn)
        }
    }

    suspend fun pinConfirm(pin: String, inn: String): PinConfirmResult {
        return authApi.pinConfirm(PinInfo(pin, inn, SdkConfig.appName))
    }

    suspend fun employeePinConfirm(
        pin: String,
        orgInn: String,
        inn: String
    ): EmployeePinConfirmResult {
        return authApi.employeePinConfirm(EmployeePinInfo(pin, orgInn, inn, SdkConfig.appName))
    }

    suspend fun closeSession(sessionId: String) {
        return authApi.closeSession(sessionId)
    }

    suspend fun uploadUserPhoto(absolutePath: String): DefaultResponse {
        Utils.compressAndResize(absolutePath)
        return authApi.uploadUserPhoto(SdkHelper.sessionId, fileAsMultipart(absolutePath))
    }

    private fun fileAsMultipart(filename: String): MultipartBody.Part {
        val reqFile = File(filename).asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", filename, reqFile)
    }

    suspend fun authByPhone(phone: String?): AuthByPhoneResult? {
        return authApi.authByPhone(phone)
    }

    suspend fun checkSmsCode(phone: String, pin: String, appName: String): PinConfirmResult {
        return authApi.checkSmsCode(AuthSmsConfirmation(phone, pin, appName))
    }

    suspend fun checkSecretWord(sessionId: String, secretWord: String): DefaultResponse {
        return authApi.checkSecretWord(sessionId, SetSecretWord(secretWord))
    }
}