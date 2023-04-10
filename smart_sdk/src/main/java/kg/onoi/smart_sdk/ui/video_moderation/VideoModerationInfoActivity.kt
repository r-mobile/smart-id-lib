package kg.onoi.smart_sdk.ui.video_moderation

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.tbruyelle.rxpermissions2.RxPermissions
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.gone
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.extensions.startActivity
import kg.onoi.smart_sdk.extensions.visible
import kg.onoi.smart_sdk.models.ModerationSchedule
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.models.VideoModerationEvent
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_video_moderation.*

class VideoModerationInfoActivity : BaseActivity<VideoModerationVM>(
    R.layout.activity_video_moderation,
    VideoModerationVM::class
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAllowBackAndHome = true
        initView()
        subscribeToLiveData()
        viewModel.fetchVideoModerationState(SdkHelper.sessionId)
    }

    private fun initView() {
        cv_toolbar.init(this, "Идентификация") { onBackPressed() }
        btn_later.setOnClickListener { onBackPressed() }
        btn_call_now.setOnClickListener { fetchVideoModerationResponse() }
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            when (it) {
                is VideoModerationEvent.UpdateModerationScheduleInfo -> updateViewState(it.info)
                is VideoModerationEvent.VideoModerationLinkFetched -> VideoCallRequestActivity.start(
                    this,
                    it.info
                )
            }
        })
    }

    private fun fetchVideoModerationResponse() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .subscribe(
                { granted -> if (granted) viewModel.fetchVideoModerationResponse(SdkHelper.sessionId) },
                { showSimpleAlert(getString(R.string.missing_permission_message)) })
    }

    private fun updateViewState(schedule: ModerationSchedule) {
        tv_work_info.text = schedule.schedule
        tv_work_info.isVisible = !schedule.schedule.isNullOrEmpty()
        if (schedule.availableRightNow) setupAsAvailableNow()
        else setupAsNotAvailableNow()
    }

    private fun setupAsAvailableNow() {
        tv_operator_will_contact.gone()
        btn_later.visible()
    }

    private fun setupAsNotAvailableNow() {
        tv_operator_will_contact.visible()
        btn_later.gone()
        btn_call_now.apply {
            text = getString(R.string.complete)
            setOnClickListener { onBackPressed() }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        SdkHelper.checkStatusCallback.invoke(ModerationStatus.WAIT_VIDEO_MODERATION)
    }

    companion object {
        fun start(context: Context) = context.startActivity<VideoModerationInfoActivity>()
    }
}