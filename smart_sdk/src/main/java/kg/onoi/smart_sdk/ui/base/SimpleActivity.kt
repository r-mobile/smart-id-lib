package kg.onoi.smart_sdk.ui.base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.showConfirmDialog
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.models.User
import kg.onoi.smart_sdk.utils.LocaleHelper
import kotlinx.android.synthetic.main.custom_toolbar.*
import org.parceler.Parcels
import kotlin.reflect.KClass

abstract class SimpleActivity(@LayoutRes open val layoutRes: Int) : AppCompatActivity(),
    OnCancelRegisterListener {

    private val CLOSE_ACTION = "kg.onoi.smart_registration.CLOSE_ACTION"
    val user: User by lazy { Parcels.unwrap<User>(intent.getParcelableExtra(Constant.Extra.USER)) }
    var isAllowBackAndHome: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        setupCloseReceiver()
    }

    fun setupToolbar(@StringRes stringRes: Int) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setTitle(stringRes)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase!!))
    }

    protected fun setupCloseReceiver() {
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    finish()
                }
            }, IntentFilter(CLOSE_ACTION)
        )
    }

    override fun onBackPressed() {
        showFinishRegisterProcessQuery()
    }

    fun closeActivityStack() {
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(CLOSE_ACTION))
    }

    override fun showFinishRegisterProcessQuery() {
        when (isAllowBackAndHome) {
            true -> closeActivityStack()
            else -> showConfirmDialog(
                null,
                getString(R.string.cancel_registration_message),
                getString(R.string.yes), getString(R.string.no), { closeActivityStack() }
            )
        }
    }

    protected fun isStartForModeration(): Boolean =
        intent.getBooleanExtra(Constant.Extra.FOR_MODERATION, false)

    fun createIntent(kClass: KClass<*>): Intent = Intent(this, kClass.java).apply {
        putExtra(Constant.Extra.USER, Parcels.wrap(user))
    }

    protected fun finishWithOkResult() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}