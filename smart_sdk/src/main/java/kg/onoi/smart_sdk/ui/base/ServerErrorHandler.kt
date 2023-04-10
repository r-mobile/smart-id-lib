package kg.onoi.smart_sdk.ui.base

import android.content.Context
import android.widget.Toast
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.ServerError
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class ServerErrorHandler(val contex: Context) {
    private fun noInternetConnection() {
        Toast.makeText(contex, contex.getString(R.string.no_internet_error), Toast.LENGTH_LONG).show()
    }

    private fun handleTimeout() {
        Toast.makeText(contex, contex.getString(R.string.timeout_error), Toast.LENGTH_LONG).show()
    }

    private fun handleTokenExpired() {
        Toast.makeText(contex, contex.getString(R.string.token_expire_error), Toast.LENGTH_LONG).show()
    }

    private fun handleCommonException(message: String? = null) {
        if(message.isNullOrEmpty()){ handleUnknownError(); return }
        Toast.makeText(contex, message, Toast.LENGTH_LONG).show()
    }

    private fun handleUnknownError() {
        Toast.makeText(contex, contex.getString(R.string.unknown_error), Toast.LENGTH_LONG).show()
    }

    private fun handleHttpException(e: HttpException) {
        try {
            val code = e.code()
            val message = e.response()?.errorBody()?.string()
            when (code) {
                401 -> handleTokenExpired()
                else -> handleCommonException(parseMessage(message))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseMessage(message: String?): String? {
        return if (message.isNullOrEmpty()) null
        else {
            try {
                JSONObject(message).getString("message")
            } catch (e: Exception) {
                message
            }
        }
    }

    fun handle(e: Throwable) {
        e.printStackTrace()
        when (e) {
            is CancellationException -> {}
            is HttpException -> handleHttpException(e)
            is UnknownHostException, is ConnectException -> noInternetConnection()
            is TimeoutException, is SocketTimeoutException -> handleTimeout()
            is ServerError -> handleCommonException(e.message)
            else -> handleCommonException()
        }
    }
}