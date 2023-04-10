package kg.onoi.smart_sdk

import android.content.Context
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.ui.close.CloseSessionActivity
import kg.onoi.smart_sdk.ui.face_detection.AuthFaceDetectionActivity
import kg.onoi.smart_sdk.ui.auth_by_inn.MainInnActivity
import kg.onoi.smart_sdk.ui.auth_by_phone.AuthPhoneActivity
import kg.onoi.smart_sdk.ui.registration_by_phone.RegistrationPhoneActivity
import kg.onoi.smart_sdk.ui.registration_complete.RegistrationStatusActivity
import kg.onoi.smart_sdk.utils.*

object SmartID {
    fun setup(sdkConfig: Config) {
        SdkConfig.setup(sdkConfig)
    }

    fun signIn(context: Context, type: SignType = SignType.COMMON, callback: ((String, ResponseWay) -> Unit)?) {
        val errorMessage = when {
            callback == null -> "The 'callback' can't be NULL"
            SdkConfig.isDetermined.not() -> "Call SmartAuth.setup() before use SmartAuth.signIn()"
            else -> null
        }
        if (!errorMessage.isNullOrEmpty()) throw RuntimeException("SmartAuth: $errorMessage")
        SdkHelper.actionCallback = callback!!
        SdkHelper.signType = type
        when (SdkHelper.signType) {
            SignType.COMMON, SignType.INDIVIDUAL -> AuthPhoneActivity.start(context)
            SignType.CORPORATE -> MainInnActivity.start(context)
        }
    }

    fun faceRecognition(context: Context, sessionId: String?, callback: ((Boolean) -> Unit)?) {
        val errorMessage = when {
            callback == null -> "The 'callback' can't be NULL"
            sessionId.isNullOrEmpty() -> "The 'sessionId' can't be empty or NULL"
            SdkConfig.isDetermined.not() -> "Call SmartAuth.setup() before use SmartAuth.faceRecognition()"
            else -> null
        }
        if (!errorMessage.isNullOrEmpty()) throw RuntimeException("SmartAuth: $errorMessage")
        AuthFaceDetectionActivity.start(context, sessionId!!, callback!!)
    }

    fun logout(context: Context, sessionId: String?, callback: (() -> Unit)?) {
        val errorMessage = when {
            callback == null -> "The 'callback' can't be NULL"
            sessionId.isNullOrEmpty() -> "The field 'sessionId' can't be empty or NULL"
            SdkConfig.isDetermined.not() -> "Call SmartAuth.setup() before use SmartAuth.logout()"
            else -> null
        }
        if (!errorMessage.isNullOrEmpty()) throw RuntimeException("SmartAuth: $errorMessage")
        CloseSessionActivity.start(context, sessionId!!, callback!!)
    }

    fun signUp(context: Context, callback: (String, ResponseWay) -> Unit) {
        val errorMessage = when {
            callback == null -> "The 'callback' can't be NULL"
            SdkConfig.isDetermined.not() -> "Call SmartRegistration.setup() before use SmartRegistration.start()"
            else -> null
        }
        if (!errorMessage.isNullOrEmpty()) throw RuntimeException("SmartRegistration: $errorMessage")
        SdkHelper.actionCallback = callback
        RegistrationPhoneActivity.start(context)
    }

    fun checkStatus(context: Context, sessionId: String, callback: (ModerationStatus) -> Unit) {
        val errorMessage = when {
            callback == null -> "The 'callback' can't be NULL"
            sessionId.isNullOrEmpty() -> "The 'sessionId' can't be empty or NULL"
            SdkConfig.isDetermined.not() -> "Call SmartRegistration.setup() before use SmartRegistration.check()"
            else -> null
        }
        if (!errorMessage.isNullOrEmpty()) throw RuntimeException("SmartRegistration: $errorMessage")
        RegistrationStatusActivity.start(context, sessionId, callback)
    }
}