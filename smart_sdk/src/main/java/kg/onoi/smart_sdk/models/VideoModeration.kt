package kg.onoi.smart_sdk.models

import com.google.gson.annotations.SerializedName
import org.parceler.Parcel

data class ModerationSchedule(val schedule: String?, val availableRightNow: Boolean)

@Parcel
data class VideoModerationResponse(
    val requestId: Int = 0,
    val videoConferenceUrl: String? = null,
    val currentlyNotAvailable: Boolean = false
) {
    fun isNotAvailable() = currentlyNotAvailable && requestId > 0
}

enum class VideoModerationStatus {
    @SerializedName("waitingOperator")
    WAITING_OPERATOR,
    @SerializedName("activeConversation")
    ACTIVE_CONVERSATION,
    @SerializedName("responseTimeout")
    RESPONSE_TIMEOUT,
    @SerializedName("conversationIsOver")
    CONVERSATION_IS_OVER
}