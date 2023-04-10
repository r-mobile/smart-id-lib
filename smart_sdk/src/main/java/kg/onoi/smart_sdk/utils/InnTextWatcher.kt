package kg.onoi.smart_sdk.utils

import android.content.Context
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import kg.onoi.smart_sdk.extensions.cursorToEnd
import kg.onoi.smart_sdk.extensions.empty
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.ui.base.BaseTextWatcher

class InnTextWatcher(private val editText: EditText, val context: Context) : BaseTextWatcher() {
    override fun afterTextChanged(s: Editable?) {
        super.afterTextChanged(s)
        if (s.isNullOrEmpty()) return

        if (!isValidFirst(s.first())) {
            editText.setText(String.empty)
            Toast.makeText(context, context.getString(R.string.invalid_lead_symbol), Toast.LENGTH_LONG).show()
        }

        if (s.length > Constant.Length.INN) {
            editText.setText(s.substring(0, Constant.Length.INN))
            editText.cursorToEnd()
        }
    }

    private fun isValidFirst(char: Char?): Boolean {
        return char in arrayOf('0', '1', '2', '3', '4')
    }
}