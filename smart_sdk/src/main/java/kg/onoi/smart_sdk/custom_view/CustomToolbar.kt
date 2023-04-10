package kg.onoi.smart_sdk.custom_view

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.ui.base.OnCancelRegisterListener
import kotlinx.android.synthetic.main.custom_toolbar.view.*

class CustomToolbar(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    init {
        LayoutInflater.from(context).inflate(R.layout.custom_toolbar, this, true)
    }

    fun init(
        activity: AppCompatActivity, @StringRes titleRes: Int,
        onBackClick: (() -> Unit) = {
            (activity as? OnCancelRegisterListener)?.showFinishRegisterProcessQuery()
        }
    ) {
        init(activity, context.getString(titleRes), onBackClick)
    }

    fun init(
        activity: AppCompatActivity, title: String? = null,
        onBackClick: (() -> Unit) = {
            (activity as? OnCancelRegisterListener)?.showFinishRegisterProcessQuery()
        }
    ) {
        tv_toolbar_title.text = title
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.apply {
            setNavigationOnClickListener { onBackClick() }
        }
    }

    fun hideBackButton() {
        toolbar.navigationIcon = null
    }
}