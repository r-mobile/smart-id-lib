package kg.onoi.smart_sdk.ui.secret_word

import android.os.Bundle
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.widget.textChanges
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.invisible
import kg.onoi.smart_sdk.extensions.showToast
import kg.onoi.smart_sdk.extensions.visible
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kotlinx.android.synthetic.main.activity_secret_word.*
import kotlin.reflect.KClass

abstract class BaseSecretWordActivity<T : BaseViewModel>(clazz: KClass<T>) :
    BaseActivity<T>(R.layout.activity_secret_word, clazz) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            when (it) {
                is Event.Success -> handleSuccessProcess()
                is Event.Fail -> {
                    showToast("Введенное слово указано не верно")
                    set_secret_word.clear()
                }
            }
        })
    }

    private fun setupViews() {
        if (isRegistration()) {
            cv_toolbar.init(this, "Секретное слово")
            tv_title.text = "Придумайте секретное слово"
            tv_top_description.visible()
            tv_bottom_description.text = "Запомните секретное слово\nи\nНИКОМУ его не сообщайте"
        } else {
            cv_toolbar.init(this, "")
            tv_title.text = "Введите секретное слово"
            tv_top_description.invisible()
            tv_bottom_description.text = "Забыли секретное слово?\nОбратитесь в Техподдержку"
        }
        set_secret_word
            .getEditText()
            .textChanges()
            .map { it.length >= 4 }
            .subscribe { btn_next.isEnabled = it }
        btn_next.setOnClickListener { onSaveBtnClick() }
    }

    abstract fun onSaveBtnClick()

    abstract fun handleSuccessProcess()

    abstract fun isRegistration(): Boolean
}