package kg.onoi.smartid

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kg.onoi.smart_sdk.SmartID
import kg.onoi.smart_sdk.utils.Config
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val apiKey = "55AA388E-0501-4AD8-B8B8-F9A0C44EC27E"//"F550C5F2-06D3-4A0A-8743-9DC3C0C4610A"//"AC7D9FC3-45F5-42DD-B227-40C639505A2E"
    val authHost = "https://smartidkg.onoi.kg/"
    val appName = "TEST_APP_AUTH"

    val regHost = "https://smartidregistration.onoi.kg/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val logo = ContextCompat.getDrawable(this, R.drawable.img_logo)
        SmartID.setup(Config(this, logo, appName, regHost, authHost, apiKey, enableRegistration = true))

        run_reg.setOnClickListener {
            //VideoCallRequestActivity.start(this) //"307c1f22-c0f7-4b06-893c-1b1f7457fbcd")
            SmartID.signUp(this) { session, _ -> tv_reg_session.text = session }
        }
        run_check.setOnClickListener {
            tv_reg_session.text = "85954616-fb32-4712-8002-c4264131803b"
            if (tv_reg_session.text.toString().isNullOrEmpty()) {
                toast("НЕТ СЕССИИ ДЛЯ ПРОВЕРКИ - НАЧНИТЕ С RUN REG")
                return@setOnClickListener
            }
            SmartID.checkStatus(this, tv_reg_session.text.toString()) {
                tv_reg_status.text = it.name
            }
        }
        run_auth.setOnClickListener {
            SmartID.signIn(this) { session, type ->
                tv_auth_session.text = "$session type ${type.name}"
            }
        }
        run_close.setOnClickListener {
            if (tv_auth_session.text.toString().isNullOrEmpty()) {
                toast("НЕТ СЕССИИ ДЛЯ ЗАКРЫТИЯ - НАЧНИТЕ С RUN AUTH")
                return@setOnClickListener
            }
            SmartID.logout(this, tv_auth_session.text.toString()) {
                tv_auth_session.text = ""
            }
        }
        btn_blinking.isVisible = false
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
