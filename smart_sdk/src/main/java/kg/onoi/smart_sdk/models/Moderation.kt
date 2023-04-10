package kg.onoi.smart_sdk.models

import com.google.gson.annotations.SerializedName
import org.parceler.Parcel

@Parcel
data class Moderation(val status: ModerationStatus = ModerationStatus.NOT_FOUND, val userData: RecognitionInfo? = null, val moderationResult: ModerationResult? = null)

enum class ModerationStatus {
    @SerializedName("Processing")
    PROCESSING,
    @SerializedName("Confirmed", alternate = ["UserRegistered"])
    CONFIRMED,
    @SerializedName("Refused", alternate = ["CancelRegistration", "RequestRejectedByGRS"])
    REFUSED,
    @SerializedName("NotFound")
    NOT_FOUND,
    @SerializedName("WaitVideoModeration")
    WAIT_VIDEO_MODERATION,
    INTERNAL_ERROR
}

@Parcel
data class ModerationResult(
    val comment: String = "",
    val failedPassportFront: Boolean = false,
    val failedPassportBack: Boolean = false,
    val failedVideo: Boolean = false,
    val failedUserPhotoWithPassport: Boolean = false,
    val failedUserPhoto: Boolean = false,
    val failedPassportInfo: Boolean = false
)