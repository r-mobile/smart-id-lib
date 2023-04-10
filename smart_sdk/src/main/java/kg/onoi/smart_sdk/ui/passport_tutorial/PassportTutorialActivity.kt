package kg.onoi.smart_sdk.ui.passport_tutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kotlinx.android.synthetic.main.activity_passport_tutorial.*

class PassportTutorialActivity : SimpleActivity(R.layout.activity_passport_tutorial) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_cancel.setOnClickListener { finish() }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PassportTutorialActivity::class.java))
        }
    }
}