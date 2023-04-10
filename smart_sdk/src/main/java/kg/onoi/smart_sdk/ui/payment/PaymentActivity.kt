package kg.onoi.smart_sdk.ui.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kotlinx.android.synthetic.main.activity_payment.*

class PaymentActivity : SimpleActivity(R.layout.activity_payment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomSheet()
    }

    private fun initBottomSheet() {
        btn_pay_now.setOnClickListener { showPaymentDialog() }
        btn_pay_later.setOnClickListener { updateViews() }
    }

    private fun updateViews() {
        tv_title.text = getString(R.string.payment_later_message)
        btn_pay_later.setText(R.string.close)
        btn_pay_later.setOnClickListener { closeActivityStack() }
    }

    private fun showPaymentDialog() {
        //PaymentBSDialog().show(supportFragmentManager, "Payment")
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PaymentActivity::class.java))
        }
    }
}
