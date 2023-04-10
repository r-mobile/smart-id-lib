package kg.onoi.smart_sdk.utils

import android.widget.EditText
import kg.onoi.smart_sdk.extensions.cursorToEnd
import kg.onoi.smart_sdk.ui.base.BaseTextWatcher

class KgPhoneTextWatcher(private val editText: EditText) : BaseTextWatcher() {

    private val baseCode = "+996 "
    private val whiteSpaceIndex = arrayOf(8, 12)
    val numberLength = 16
    private var isRemoveNow = false

    init {
        setText(baseCode)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        super.beforeTextChanged(s, start, count, after)
        isRemoveNow = count > after
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        super.onTextChanged(s, start, before, count)
        if (s!!.length < baseCode.length) {
            setText(baseCode); return
        }

        if (isRemoveNow) {
            if (s.length - 1 in whiteSpaceIndex) {
                setText(s.substring(0, s.length - 1))
            }
            return
        }

        val text = s.toString()
        text.run {
            when {
                start in whiteSpaceIndex -> setText("${this.substring(0, start)} ${this[length - 1]}")
                length in whiteSpaceIndex -> setText("$this ")
                length > numberLength -> setText(substring(0, numberLength))
            }
        }
    }

    private fun setText(string: String) {
        editText.setText(string)
        editText.cursorToEnd()
    }
}