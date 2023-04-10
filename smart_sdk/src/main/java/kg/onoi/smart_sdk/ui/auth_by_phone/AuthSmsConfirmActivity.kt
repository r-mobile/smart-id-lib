package kg.onoi.smart_sdk.ui.auth_by_phone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.widget.textChanges
import com.tbruyelle.rxpermissions2.RxPermissions
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.*
import kg.onoi.smart_sdk.models.AuthByPhoneEvents
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.face_detection.AuthFaceDetectionActivity
import kg.onoi.smart_sdk.utils.ResponseWay
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_sms_register.*
import java.util.concurrent.TimeUnit

class AuthSmsConfirmActivity :
    BaseActivity<AuthVM>(R.layout.activity_sms_register, AuthVM::class) {

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        subscribeToLiveData()
        startCountdownTimer()
    }

    @SuppressLint("CheckResult")
    private fun initView() {
        cv_toolbar.init(this) { onBackPressed() }
        et_pin.textChanges().subscribe { btn_next.isEnabled = it.length >= 4 }
        btn_next.setOnClickListener {
            viewModel.checkSmsCode(
                intent.getStringExtra(String::class.java.canonicalName) ?: "",
                et_pin.getString()
            )
        }
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is AuthByPhoneEvents.NonApprovedResponseEvent -> {
                        showSimpleAlert(getString(R.string.invalid_pin))
                        et_pin.text?.clear()
                    }
                    is AuthByPhoneEvents.SuccessSessionResponseEvent -> callCallbackAndClose(it.sessionId)
                    is AuthByPhoneEvents.InvalidSessionResponseEvent -> showToast(getString(R.string.unknow_exception))
                    is AuthByPhoneEvents.SuccessSmsSend -> {
                        startCountdownTimer()
                        showToast(getString(R.string.success_resend_sms))
                    }
                    is AuthByPhoneEvents.RequestFaceRecognitionEvent -> {
                        SdkHelper.sessionId = it.sessionId
                        startFaceRecognitionWithCheckPermission(it.needSecretWord)
                    }
                    else -> {
                    }
                }
            }
        })
    }

    private fun startCountdownTimer() {
        setupViewsBeforeStartTimer()
        countDownTimer = object : CountDownTimer(TimeUnit.SECONDS.toMillis(60), 1000) {
            override fun onFinish() {
                setupViewForStoppedTimer()
            }

            override fun onTick(millisUntilFinished: Long) {
                updateTimerTick(millisUntilFinished.toMinuteSecondTime())
            }
        }.start()
    }

    private fun updateTimerTick(time: String) {
        tv_resend_sms.text = getString(R.string.sms_repeate_timeout_format, time)
    }

    private fun setupViewsBeforeStartTimer() {
        tv_resend_sms.setTextColor(ContextCompat.getColor(this, R.color.sm_text_color))
        tv_resend_sms.setOnClickListener(null)
    }

    private fun setupViewForStoppedTimer() {
        tv_resend_sms.setTextColor(ContextCompat.getColor(this, R.color.sm_control_color))
        tv_resend_sms.setOnClickListener { onResendSmsClick() }
        tv_resend_sms.text = getString(R.string.repeat_sms)
    }

    private fun onResendSmsClick() {
        viewModel.sendConfirmSms(intent.getStringExtra(String::class.java.canonicalName) ?: "")
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    private fun callCallbackAndClose(sessionId: String) {
        SdkHelper.actionCallback.invoke(sessionId, ResponseWay.AUTH)
        closeActivityStack()
    }

    private fun startFaceRecognitionWithCheckPermission(needSecretWord: Boolean) {
        RxPermissions(this)
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe {
                when {
                    it.granted -> {
                        closeActivityStack(); AuthFaceDetectionActivity.start(this, needSecretWord)
                    }
                    it.shouldShowRequestPermissionRationale -> showRationaleMessage { showApplicationSettings() }
                    else -> showRationaleMessage { startFaceRecognitionWithCheckPermission(needSecretWord) }
                }
            }
    }

    private fun showRationaleMessage(action: () -> Unit) {
        showSimpleAlert(getString(R.string.take_permission_message)) {
            action()
        }
    }

    companion object {
        fun start(context: Context, phone: String) {
            context.startActivity(Intent(context, AuthSmsConfirmActivity::class.java).apply {
                putExtra(String::class.java.canonicalName, phone)
            })
        }
    }
}
