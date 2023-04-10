package kg.onoi.smart_sdk.ui.success_registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.ModerationStatus
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_success_registration.*

class SuccessRegistrationActivity : SimpleActivity(R.layout.activity_success_registration) {

    private val checkedChangeListener =
        CompoundButton.OnCheckedChangeListener { _, _ ->
            btn_complete.isEnabled = cb_certificate.isChecked
                    && cb_offer.isChecked
                    && cb_puc.isChecked
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAllowBackAndHome = true
        setupViews()
    }

    private fun setupViews() {
        cb_certificate.setOnCheckedChangeListener(checkedChangeListener)
        cb_offer.setOnCheckedChangeListener(checkedChangeListener)
        cb_puc.setOnCheckedChangeListener(checkedChangeListener)
        btn_complete.setOnClickListener {
            closeActivityStack()
            SdkHelper.checkStatusCallback.invoke(ModerationStatus.CONFIRMED)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SuccessRegistrationActivity::class.java))
        }
    }
}