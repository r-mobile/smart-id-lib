package kg.onoi.smart_sdk.models

import com.google.gson.annotations.SerializedName


data class RegistrationSettings(
    val steps: List<RegistrationSteps>,
    val useMainCamera: Boolean,
    val checkSmile: Boolean = true,
    val checkEyes: Boolean = true,
    @SerializedName("needFillRegistrationAddress")
    val needFillAddress: Boolean = true
) {
    fun isEnableExpressionTracking() = checkEyes && checkSmile
}

enum class RegistrationSteps {
    @SerializedName("none")
    NONE,

    @SerializedName("secretWord")
    SECRET_WORD,

    @SerializedName("userPhoto")
    USER_PHOTO,

    @SerializedName("passportFront")
    PASSPORT_FRONT,

    @SerializedName("passportBack")
    PASSPORT_BACK,

    @SerializedName("userPhotoWithPassport")
    PHOTO_WITH_PASSPORT,
}