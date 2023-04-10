package kg.onoi.smart_sdk.ui.registration_confirmation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.widget.CheckBox
import androidx.core.text.parseAsHtml
import androidx.lifecycle.Observer
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.ConfirmationDocument
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.utils.ResponseWay
import kg.onoi.smart_sdk.utils.SdkConfig
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_registration_confirmation.*
import kotlinx.android.synthetic.main.item_checkbox.view.*

class RegistrationConfirmationActivity : BaseActivity<RegistrationConfirmationVM>(
    R.layout.activity_registration_confirmation,
    RegistrationConfirmationVM::class
) {

    private val checkBoxes = mutableListOf<CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        subscribeToLivedata()
        viewModel.fetchCheckBoxes(intent.getStringExtra(String::class.java.canonicalName) ?: "")
    }

    private fun subscribeToLivedata() {
        viewModel.documents.observe(this, Observer {
            setupCheckBoxes(it)
        })
        viewModel.title.observe(this, Observer {
            tv_description.text = when {
                it.isNullOrEmpty() -> getString(R.string.registration_complete_desc)
                else -> it
            }
        })
    }

    private fun setupCheckBoxes(docs: List<ConfirmationDocument>) {
        ll_container.apply {
            removeAllViews()
            for (doc in docs) {
                val view = LayoutInflater.from(this@RegistrationConfirmationActivity)
                    .inflate(R.layout.item_checkbox, null, false)
                view.apply {
                    checkBoxes.add(cb_confirm)
                    cb_confirm.setOnCheckedChangeListener { _, _ -> updateButtonEnabled() }
                    tv_confirm.apply {
                        text = buildLink(doc).parseAsHtml()
                        setMovementMethod(LinkMovementMethod.getInstance())
                    }
                }
                addView(view)
            }
        }

    }

    private fun buildLink(doc: ConfirmationDocument): String {
        val sessionId = intent.getStringExtra(String::class.java.canonicalName) ?: ""
        val url =
            "${SdkConfig.registrationHost}api/Documents/GetDocument/${sessionId}/${doc.documentType}/${SdkConfig.apiKey}"
        return "${doc.text} <a href=\"$url\">${doc.linkText}</a>"
    }

    private fun setupViews() {
        ct_toolbar.init(this)
        button.setOnClickListener {
            SdkHelper.actionCallback.invoke(
                intent.getStringExtra(String::class.java.canonicalName) ?: "",
                ResponseWay.REGISTRATION
            )
            closeActivityStack()
        }
    }

    private fun updateButtonEnabled() {
        button.isEnabled = checkBoxes.all { it.isChecked }
    }

    companion object {
        fun start(context: Context, sessionId: String) {
            val intent = Intent(context, RegistrationConfirmationActivity::class.java).apply {
                putExtra(String::class.java.canonicalName, sessionId)
            }
            context.startActivity(intent)
        }
    }
}