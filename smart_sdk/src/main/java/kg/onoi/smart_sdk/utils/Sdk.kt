package kg.onoi.smart_sdk.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.Patterns
import com.google.android.gms.vision.CameraSource
import kg.onoi.smart_sdk.extensions.asMd5
import kg.onoi.smart_sdk.extensions.empty
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.models.RegistrationSettings
import kg.onoi.smart_sdk.models.RegistrationSteps
import java.util.*


object SdkConfig {
    var deviceId: String = "0"
        private set
    var logo: Drawable? = null
        private set
    var appName: String = String.empty
        private set
    var registrationHost: String = String.empty
        private set
    var authHost: String = String.empty
        private set
    var language: String = "ru"
        private set
    var apiKey: String = String.empty
        private set
    var isDetermined: Boolean = false
        private set
    var enableRegistration: Boolean = false
        private set

    fun setup(config: Config) {
        this.logo = config.logo
        this.appName = config.appName
        this.registrationHost = config.registrationHost
        this.authHost = config.authHost
        this.apiKey = config.apiKey
        this.language = config.language
        this.enableRegistration = config.enableRegistration
        calculateDeviceID(config.appContext)
        isDetermined = isValidConfig()
    }

    private fun isValidConfig(): Boolean {
        return appName.isNullOrEmpty().not()
                && apiKey.isNullOrEmpty().not()
                && authHost.isNullOrEmpty().not()
                && Patterns.WEB_URL.matcher(authHost).matches()
                && registrationHost.isNullOrEmpty().not()
                && Patterns.WEB_URL.matcher(registrationHost).matches()
    }

    private fun calculateDeviceID(context: Context) {
        deviceId = (Settings
            .Secure
            .getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            .takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()).asMd5()
            .let { "$deviceId" }
    }
}

object SdkHelper {
    var settings: RegistrationSettings = RegistrationSettings(
        listOf(
            RegistrationSteps.USER_PHOTO,
            RegistrationSteps.PASSPORT_BACK,
            RegistrationSteps.PASSPORT_FRONT,
            RegistrationSteps.SECRET_WORD,
            RegistrationSteps.PHOTO_WITH_PASSPORT
        ), false
    )
    var signType: SignType = SignType.COMMON
    var sessionId: String = ""
    var actionCallback: (String, ResponseWay) -> Unit = { _, _ -> }
    var checkStatusCallback: (ModerationStatus) -> Unit = {}
    var faceRecognitionCallback: (Boolean) -> Unit = {}
    var logoutCallback: () -> Unit = {}
    var cameraType = CameraSource.CAMERA_FACING_FRONT

    fun isContainStep(step: RegistrationSteps) = settings.steps.contains(step)

    fun isEnableExpressionTracking() = settings.isEnableExpressionTracking()
}

data class Config(
    val appContext: Context,
    val logo: Drawable? = null,
    val appName: String,
    val registrationHost: String,
    val authHost: String,
    val apiKey: String,
    val language: String = "ru",
    val enableRegistration: Boolean = false
)

enum class SignType {
    COMMON,
    INDIVIDUAL,
    CORPORATE
}

enum class ResponseWay {
    REGISTRATION,
    AUTH
}