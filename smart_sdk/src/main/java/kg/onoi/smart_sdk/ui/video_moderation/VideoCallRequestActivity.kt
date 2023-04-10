package kg.onoi.smart_sdk.ui.video_moderation

import android.Manifest
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.tbruyelle.rxpermissions2.RxPermissions
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.*
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.models.PhotoType
import kg.onoi.smart_sdk.models.VideoModerationResponse
import kg.onoi.smart_sdk.models.VideoModerationStatus
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_video_call_request.*
import org.parceler.Parcels

class VideoCallRequestActivity : BaseActivity<VideoModerationVM>(
    R.layout.activity_video_call_request,
    VideoModerationVM::class
) {

    var isBusyNow = false

    private val moderationResponse by lazy {
        Parcels.unwrap<VideoModerationResponse>(intent.getParcelableExtra(VideoModerationResponse::class.java.canonicalName))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAllowBackAndHome = true
        initView()
        subscribeToLiveData()
        if (moderationResponse.isNotAvailable()) setupAsOperatorsBusy()
        else {
            setupAsWaitOperator(); getVideoModerationStatus()
        }
    }

    private fun getVideoModerationStatus(delay: Long = 0) {
        viewModel.getVideoModerationStatus(SdkHelper.sessionId, moderationResponse.requestId, delay)
    }

    private fun initView() {
        cv_toolbar.init(this, "Видеозвонок") { onBackPressed() }
        ll_busy_operators.gone()
        btn_done.setOnClickListener { onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.videoModerationStatus.observe(this, Observer {
            when (it) {
                VideoModerationStatus.RESPONSE_TIMEOUT -> setupAsOperatorsBusy()
                VideoModerationStatus.ACTIVE_CONVERSATION ->
                    moderationResponse.videoConferenceUrl?.let {
                        VideoCallActivity.start(this, it, moderationResponse.requestId)
                        finish()
                    }
                else -> getVideoModerationStatus(5000)
            }
        })
    }

    private fun setupAsWaitOperator() {
        iv_call_process.apply {
            setBackgroundResource(R.drawable.call_animation)
            (background as? AnimationDrawable)?.start()
        }
    }

    private fun setupAsOperatorsBusy() {
        isBusyNow = true
        tv_header.text = "Извините,\nвсе операторы заняты."
        ll_busy_operators.visible()
        iv_call_process.gone()
    }

    override fun onBackPressed() {
        if (isBusyNow) {
            super.onBackPressed()
            SdkHelper.checkStatusCallback.invoke(ModerationStatus.WAIT_VIDEO_MODERATION)
        } else {
            showConfirmDialog(
                message = "Вы действительно хотите прервать видеозвонок?",
                okLabel = getString(R.string.yes),
                cancelLabel = getString(R.string.no),
                onOkClick = { finish() })
        }
    }

    companion object {
        fun start(
            context: Context,
            info: VideoModerationResponse
        ) = context.startActivity<VideoCallRequestActivity> {
            putExtra(VideoModerationResponse::class.java.canonicalName, Parcels.wrap(info))
        }
    }
}