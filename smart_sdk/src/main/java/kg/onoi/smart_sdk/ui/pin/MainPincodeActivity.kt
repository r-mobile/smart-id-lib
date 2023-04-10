package kg.onoi.smart_sdk.ui.pin

import android.app.Activity
import android.content.Intent
import kg.onoi.smart_sdk.extensions.getString
import kg.onoi.smart_sdk.utils.ExtrasName
import kotlinx.android.synthetic.main.activity_pin_code.*

class MainPincodeActivity : BasePinActivity() {

    override fun onNextBtnClick() {
        viewModel.pinConfirm(et_pin.getString(), getInn())
    }

    override fun onRepeatClick() {
        viewModel.repeatRequest(getInn())
    }

    companion object {
        fun start(activity: Activity, inn: String, isPermanent: Boolean) {
            val intent = Intent(activity, MainPincodeActivity::class.java)
            intent.putExtra(ExtrasName.INN, inn)
            intent.putExtra(ExtrasName.IS_PERMANENT_PIN, isPermanent)
            activity.startActivityForResult(intent, 0)
        }
    }
}