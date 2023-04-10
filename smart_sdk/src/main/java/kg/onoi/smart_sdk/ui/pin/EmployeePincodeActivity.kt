package kg.onoi.smart_sdk.ui.pin

import android.app.Activity
import android.content.Intent
import kg.onoi.smart_sdk.extensions.getString
import kg.onoi.smart_sdk.utils.ExtrasName
import kotlinx.android.synthetic.main.activity_pin_code.*

class EmployeePincodeActivity : BasePinActivity() {

    override fun onNextBtnClick() {
        viewModel.employeePinConfirm(et_pin.getString(), getOrgInn()!!, getInn())
    }

    override fun onRepeatClick() {
        viewModel.repeatRequest(getOrgInn()!!, getInn())
    }

    companion object {
        fun start(activity: Activity, orgInn: String, inn: String, isPermanent: Boolean) {
            val intent = Intent(activity, EmployeePincodeActivity::class.java)
            intent.putExtra(ExtrasName.ORG_INN, orgInn)
            intent.putExtra(ExtrasName.INN, inn)
            intent.putExtra(ExtrasName.IS_PERMANENT_PIN, isPermanent)
            activity.startActivityForResult(intent, 0)
        }
    }
}