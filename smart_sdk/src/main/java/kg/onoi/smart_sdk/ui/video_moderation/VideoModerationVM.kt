package kg.onoi.smart_sdk.ui.video_moderation

import androidx.lifecycle.MutableLiveData
import kg.onoi.smart_sdk.models.VideoModerationEvent
import kg.onoi.smart_sdk.models.VideoModerationStatus
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel

class VideoModerationVM(val repo: RegistrationRepo) : BaseViewModel() {

    var videoModerationStatus = MutableLiveData<VideoModerationStatus>()

    fun fetchVideoModerationState(sessionId: String) {
        runWithProgress {
            event.value = VideoModerationEvent.UpdateModerationScheduleInfo(
                repo.getVideoModerationSchedule(sessionId)
            )
        }
    }

    fun fetchVideoModerationResponse(sessionId: String) {
        runWithProgress {
            event.value = VideoModerationEvent.VideoModerationLinkFetched(
                repo.getVideoModerationResponse(sessionId)
            )
        }
    }

    fun getVideoModerationStatus(sessionId: String, requestId: Int, delay: Long = 0) {
        safeCall {
            kotlinx.coroutines.delay(delay)
            videoModerationStatus.value = repo.getVideoModerationStatus(sessionId, requestId)
        }
    }
}