package kg.onoi.smart_sdk.ui.registration_by_phone

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding3.widget.textChanges
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.*
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.models.PhoneRegistrationEvents
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.registration_steps.RegistrationStepsActivity
import kotlinx.android.synthetic.main.activity_sms_register.*
import java.util.concurrent.TimeUnit

class RegistrationSmsConfirmationActivity :
    BaseActivity<RegistrationVM>(R.layout.activity_sms_register, RegistrationVM::class) {

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
            viewModel.checkSmsCode(user.sessionId, et_pin.getString())
        }
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is Event.Success -> startNextActivity()
                    is PhoneRegistrationEvents.SuccessSmsSend -> {
                        startCountdownTimer()
                        showToast(getString(R.string.succes_resend_sms))
                    }
                    is Event.Fail -> {
                        showToast(it.message)
                        et_pin.text?.clear()
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
        tv_resend_sms.apply {
            setTextColor(
                ContextCompat.getColor(
                    this@RegistrationSmsConfirmationActivity,
                    R.color.sm_text_color
                )
            )
            setOnClickListener(null)
            normal()
        }
    }

    private fun setupViewForStoppedTimer() {
        tv_resend_sms.apply {
            setTextColor(
                ContextCompat.getColor(
                    this@RegistrationSmsConfirmationActivity,
                    R.color.sm_control_color
                )
            )
            setOnClickListener { onResendSmsClick() }
            text = getString(R.string.repeat_sms)
            underline()
        }
    }

    private fun onResendSmsClick() {
        viewModel.sendConfirmSms(user.phoneNumber)
    }

    private fun startNextActivity() {
        RegistrationStepsActivity.start(this)
        closeActivityStack()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(activity: BaseActivity<*>) {
            activity.startActivity(activity.createIntent(RegistrationSmsConfirmationActivity::class))
        }
    }
}
