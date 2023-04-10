package kg.onoi.smart_sdk.ui.registration_complete

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.setIsVisible
import kg.onoi.smart_sdk.extensions.startActivity
import kg.onoi.smart_sdk.extensions.visible
import kg.onoi.smart_sdk.models.Moderation
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.models.RegistrationCompleteEvents
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.moderation.ModerationActivity
import kg.onoi.smart_sdk.ui.video_moderation.VideoModerationInfoActivity
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_registration_complete.*
import kotlin.math.roundToInt

class RegistrationStatusActivity : BaseActivity<RegistrationStatusVM>(
    R.layout.activity_registration_complete,
    RegistrationStatusVM::class
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAllowBackAndHome = true
        setupInProcess(false)
        subscribeToLiveData()
        viewModel.runCheckOrderStatusTimer()
        btn_complete.setOnClickListener { invokeCheckStateCallback(); closeActivityStack() }
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is RegistrationCompleteEvents.WaitingComplete -> setupInProcess()
                    is RegistrationCompleteEvents.SuccessComplete -> setupAsSuccessComplete()
                    is RegistrationCompleteEvents.RegistrationNotFound -> setupAsError()
                    is RegistrationCompleteEvents.RegistrationRefused -> handleRefused(it.result)
                    is RegistrationCompleteEvents.WaitingVideoModeration -> {
                        VideoModerationInfoActivity.start(this)
                        finish()
                    }
                    is RegistrationCompleteEvents.InternalError -> setupAsError()
                }
            }
        })
    }

    private fun setupInProcess(isCompleteBtnVisible: Boolean = true) {
        tv_title.text = "Ваши регистрационные данные проверяются"
        tv_second_message.text = "Проверка осуществляется только в рабочие дни"
        btn_complete.apply {
            text = "OK"
            setIsVisible(isCompleteBtnVisible)
        }
        iv_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_status_clock))
        updateImageViewSize(160)
    }

    private fun setupAsSuccessComplete() {
        tv_title.text = "Поздравляем"
        tv_second_message.text = "Ваша квалифицированная электронная подпись активирована"
        btn_complete.apply {
            text = "Готово"
            visible()
        }
        iv_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.success_registration))
        updateImageViewSize(300)
    }

    private fun setupAsError() {
        tv_title.text = "Ваша заявка не прошла проверку"
        tv_second_message.text = "Обратитесь в Центр Обслуживания"
        btn_complete.apply {
            text = "Готово"
            visible()
        }
        iv_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_status_warning))
        updateImageViewSize(160)
    }

    private fun updateImageViewSize(size: Int) {
        ConstraintSet().apply {
            clone(cl_root)
            constrainWidth(R.id.iv_image, dpToPx(size))
            applyTo(cl_root)
        }
    }

    private fun handleRefused(moderation: Moderation) {
        closeActivityStack()
        ModerationActivity.start(this, moderation)
    }

    private fun invokeCheckStateCallback() {
        SdkHelper.checkStatusCallback.invoke(
            viewModel.moderation?.status ?: ModerationStatus.INTERNAL_ERROR
        )
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    companion object {
        fun start(
            context: Context,
            sessionId: String,
            callback: (ModerationStatus) -> Unit
        ) {
            SdkHelper.sessionId = sessionId
            SdkHelper.checkStatusCallback = callback
            context.startActivity(Intent(context, RegistrationStatusActivity::class.java))
        }

        fun start(context: Context) = context.startActivity<RegistrationStatusActivity>()
    }
}