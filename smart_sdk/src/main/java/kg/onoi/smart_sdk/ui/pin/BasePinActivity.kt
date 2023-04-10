package kg.onoi.smart_sdk.ui.pin

import android.Manifest
import androidx.lifecycle.Observer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import android.text.InputType
import com.jakewharton.rxbinding3.widget.textChanges
import com.tbruyelle.rxpermissions2.RxPermissions
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.setIsVisible
import kg.onoi.smart_sdk.extensions.showApplicationSettings
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.extensions.showToast
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.PincodeEvent
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.face_detection.AuthFaceDetectionActivity
import kg.onoi.smart_sdk.utils.ExtrasName
import kg.onoi.smart_sdk.utils.ResponseWay
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_pin_code.*
import java.util.concurrent.TimeUnit

abstract class BasePinActivity :
    BaseActivity<PincodeVM>(R.layout.activity_pin_code, PincodeVM::class) {

    var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cv_toolbar.init(this)
        initViews()
        subscribeLiveData()
    }

    private fun fetchSmsTimeout() {
        viewModel.repeatRequestTimeout(getInn(), getOrgInn())
    }

    private fun initViews() {
        tv_repeate.setIsVisible(!isPermanentPin())

        et_pin.inputType = getPinInputType()
        if (isPermanentPin()) {
            tv_title.setText(R.string.input_pincode)
        } else {
            fetchSmsTimeout()
        }

        et_pin
            .textChanges()
            .subscribe { updateSigninState(it.length >= 4) }

        btn_next.setOnClickListener { onNextBtnClick() }
    }

    private fun getPinInputType(): Int {
        return InputType.TYPE_CLASS_NUMBER or if (isPermanentPin())
            InputType.TYPE_NUMBER_VARIATION_PASSWORD else InputType.TYPE_NUMBER_FLAG_SIGNED
    }

    private fun updateSigninState(isCorrectLength: Boolean) {
        btn_next.isEnabled = isCorrectLength
    }

    private fun subscribeLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is PincodeEvent.NonApprovedResponseEvent -> {
                        showSimpleAlert(getString(R.string.invalid_pin))
                        et_pin.text?.clear()
                    }
                    is PincodeEvent.SuccessSessionResponseEvent -> callCallbackAndClose(it.sessionId)
                    is PincodeEvent.InvalidSessionResponseEvent -> showToast(getString(R.string.unknow_exception))
                    is Event.UnknownErrorEvent -> showToast(getString(R.string.unknow_exception))
                    is Event.ErrorEvent -> showSimpleAlert(it.message)
                    is PincodeEvent.SmsRepeateSuccessEvent -> showToast(getString(R.string.success_resend_sms))
                    is PincodeEvent.RequestFaceRecognitionEvent -> {
                        SdkHelper.sessionId = it.sessionId
                        startFaceRecognitionWithCheckPermission()
                    }
                    is PincodeEvent.RepeateTimeoutEvent -> startCountdownTimer(it.timeout)
                    else -> {
                    }
                }
            }
        })
    }

    private fun startFaceRecognitionWithCheckPermission() {
        RxPermissions(this)
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe {
                when {
                    it.granted -> {
                        closeActivityStack(); AuthFaceDetectionActivity.start(this)
                    }
                    it.shouldShowRequestPermissionRationale -> showRationaleMessage { showApplicationSettings() }
                    else -> showRationaleMessage { startFaceRecognitionWithCheckPermission() }
                }
            }
    }

    private fun showRationaleMessage(action: () -> Unit) {
        showSimpleAlert(getString(R.string.take_permission_message)) {
            action()
        }
    }

    private fun startCountdownTimer(timeout: Int) {
        tv_repeate.setTextColor(ContextCompat.getColor(this, R.color.sm_text_color))
        tv_repeate.setOnClickListener(null)
        countDownTimer =
            object : CountDownTimer(TimeUnit.SECONDS.toMillis(timeout.toLong()), 1000) {
                override fun onFinish() {
                    tv_repeate.setTextColor(
                        ContextCompat.getColor(
                            this@BasePinActivity,
                            R.color.sm_control_color
                        )
                    )
                    tv_repeate.setOnClickListener { onRepeatClick() }
                    tv_repeate.text = getString(R.string.repeate_sms)
                }

                override fun onTick(millisUntilFinished: Long) {
                    val format = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                    tv_repeate.text = getString(R.string.sms_repeate_timeout_format, format)
                }
            }
        countDownTimer?.start()
    }

    private fun callCallbackAndClose(sessionId: String) {
        SdkHelper.actionCallback.invoke(sessionId, ResponseWay.AUTH)
        finishWithOkResult()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }

    fun getInn(): String = intent.getStringExtra(ExtrasName.INN)

    fun getOrgInn(): String? = intent.getStringExtra(ExtrasName.ORG_INN)

    private fun isPermanentPin() = intent.getBooleanExtra(ExtrasName.IS_PERMANENT_PIN, false)

    abstract fun onNextBtnClick()

    abstract fun onRepeatClick()
}