package kg.onoi.smart_sdk.custom_view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.textfield.TextInputLayout
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.getString
import kg.onoi.smart_sdk.extensions.showKeyboard
import kg.onoi.smart_sdk.extensions.visible
import kg.onoi.smart_sdk.ui.base.BaseTextWatcher
import kotlinx.android.synthetic.main.stated_edit_view.view.*

class StatedEditText(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {
    init {
        LayoutInflater.from(context).inflate(R.layout.stated_edit_view, this, true)
        parseStyle(attributeSet)
        et_text.addTextChangedListener(NormalizerTextWatcher())
    }

    private fun parseStyle(attributeSet: AttributeSet) {
        val styledAttributes =
            context.obtainStyledAttributes(attributeSet, R.styleable.StatedEditText)
        et_text.inputType =
            styledAttributes.getInt(
                R.styleable.StatedEditText_android_inputType,
                InputType.TYPE_CLASS_TEXT
            )

        if (styledAttributes.hasValue(R.styleable.StatedEditText_title)) {
            til_text.hint = styledAttributes.getString(R.styleable.StatedEditText_title)
        }

        if (styledAttributes.hasValue(R.styleable.StatedEditText_error)) {
            til_text.error = styledAttributes.getString(R.styleable.StatedEditText_error)
        }

        if (styledAttributes.hasValue(R.styleable.StatedEditText_counterLength)) {
            til_text.apply {
                isCounterEnabled = true
                counterMaxLength =
                    styledAttributes.getInt(R.styleable.StatedEditText_counterLength, 0)
            }
        }

        if (styledAttributes.hasValue(R.styleable.StatedEditText_needFocus)) {
            et_text.postDelayed({
                et_text.requestFocus()
                et_text.showKeyboard()
            }, 300)
        }

        if (styledAttributes.hasValue(R.styleable.StatedEditText_bottom_hint)) {
            tv_bottom_hint.apply {
                text = styledAttributes.getString(R.styleable.StatedEditText_bottom_hint)
                visible()
            }
        }

        styledAttributes.recycle()
    }

    fun addEndIcon(@DrawableRes drawableResId: Int, onClickAction: () -> Unit) {
        til_text.apply {
            endIconMode = TextInputLayout.END_ICON_CUSTOM

            endIconDrawable = ContextCompat.getDrawable(context, drawableResId)?.apply {
                setEndIconTintList(
                    ContextCompat.getColorStateList(
                        context,
                        R.color.sm_control_color
                    )
                )
                setEndIconTintMode(PorterDuff.Mode.MULTIPLY)
            }
            setEndIconOnClickListener { onClickAction() }
        }
    }

    fun setTitle(resId: Int) = setTitle(context.getString(resId))

    fun setTitle(title: String) {
        til_text.hint = title
    }

    fun getText(): String = et_text.getString()

    fun clear() = et_text.setText("")

    fun getEditText() = et_text

    fun addTextWatcher(textWatcher: TextWatcher) {
        et_text.addTextChangedListener(textWatcher)
    }

    fun length(): Int = getText().length
    fun maxLength(i: Int) {
        et_text.filters = arrayOf(InputFilter.LengthFilter(i))
    }

    fun showError(text: String) {
        til_text.apply {
            error = text
            isErrorEnabled = true
        }
    }

    fun hideError() {
        til_text.apply {
            error = null
            isErrorEnabled = false
        }
    }

    inner class NormalizerTextWatcher : BaseTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
        }
    }

}