package kg.onoi.smart_sdk.ui.registration_complete

import kg.onoi.smart_sdk.models.Moderation
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.models.RegistrationCompleteEvents
import kg.onoi.smart_sdk.repositories.RegistrationRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RegistrationStatusVM(private val repo: RegistrationRepo) : BaseViewModel() {

    private val ticker = ticker(TimeUnit.SECONDS.toMillis(10), 0)
    var sessionId: String? = SdkHelper.sessionId
    var moderation: Moderation? = null

    fun runCheckOrderStatusTimer() {
        vmCoroutineScope.launch { for (unit in ticker) updateOrderStatus() }
    }

    private suspend fun updateOrderStatus() {
        moderation = try {
            repo.fetchRegistrationStatusAsync(sessionId)
        } catch (e: Exception) {
            e.printStackTrace()
            finishTimer()
            event.value = RegistrationCompleteEvents.InternalError()
            return
        }

        event.value = when (moderation?.status) {
            ModerationStatus.CONFIRMED -> {
                finishTimer(); RegistrationCompleteEvents.SuccessComplete()
            }
            ModerationStatus.PROCESSING -> RegistrationCompleteEvents.WaitingComplete()
            ModerationStatus.REFUSED -> {
                finishTimer(); RegistrationCompleteEvents.RegistrationRefused(moderation!!)
            }
            ModerationStatus.WAIT_VIDEO_MODERATION -> {
                finishTimer(); RegistrationCompleteEvents.WaitingVideoModeration()
            }
            ModerationStatus.NOT_FOUND -> {
                finishTimer(); RegistrationCompleteEvents.RegistrationNotFound()
            }
            else -> {
                finishTimer(); RegistrationCompleteEvents.InternalError()
            }
        }
    }

    private fun finishTimer() {
        ticker.cancel()
    }
}