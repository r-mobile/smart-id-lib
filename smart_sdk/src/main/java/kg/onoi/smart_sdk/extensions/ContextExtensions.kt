package kg.onoi.smart_sdk.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kg.onoi.smart_sdk.R

fun Context.showSimpleAlert(message: String, onClick: (() -> Unit) = {}) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(R.string.ok) { _, _ -> onClick() }
        .create()
        .show()
}

fun Context.showConfirmDialog(tittle: String? = null, message: String, okLabel: String = getString(R.string.ok),
                              cancelLabel: String = getString(R.string.cancel),
                              onOkClick: (() -> Unit) = {}, onCancelClick: (() -> Unit) = {}) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setTitle(tittle)
        .setPositiveButton(okLabel) { _, _ -> onOkClick() }
        .setNegativeButton(cancelLabel) { _, _ -> onCancelClick() }
        .setCancelable(false)
        .create()
        .show()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showApplicationSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:$packageName")
    )
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

inline fun <reified T : Activity> Context.startActivity(extra: (Intent.() -> Unit) = {}) {
    startActivity(Intent(this, T::class.java).apply(extra))
}