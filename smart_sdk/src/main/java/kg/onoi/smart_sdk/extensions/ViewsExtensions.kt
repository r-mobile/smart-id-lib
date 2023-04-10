package kg.onoi.smart_sdk.extensions

import android.content.Context
import android.graphics.Paint
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView


fun View.setIsVisible(visible: Boolean) {
    if (visible) visible() else gone()
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun EditText.getString() = text.toString()

fun EditText.cursorToEnd() {
    setSelection(getString().length)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.normal() {
    paintFlags = paintFlags or Paint.LINEAR_TEXT_FLAG
}