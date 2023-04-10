package kg.onoi.smart_sdk.ui.video_moderation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.lifecycle.Observer
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.showConfirmDialog
import kg.onoi.smart_sdk.extensions.startActivity
import kg.onoi.smart_sdk.models.VideoModerationStatus
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.registration_complete.RegistrationStatusActivity
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.actvity_video_call.*

class VideoCallActivity :
    BaseActivity<VideoModerationVM>(R.layout.actvity_video_call, VideoModerationVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAllowBackAndHome = true
        setupViews()
        subscribeToLiveData()
        wv_web.loadUrl(intent.getStringExtra(String::class.java.canonicalName))
        requestStatus(0)
    }

    private fun setupViews() {
        setupWebView()
        cv_toolbar.init(this, "Видеозвонок") { onBackPressed() }
    }

    private fun subscribeToLiveData() {
        viewModel.videoModerationStatus.observe(this, Observer {
            when (it) {
                VideoModerationStatus.RESPONSE_TIMEOUT,
                VideoModerationStatus.CONVERSATION_IS_OVER -> {
                    closeActivityStack()
                    RegistrationStatusActivity.start(this)
                }
                else -> requestStatus(5000)
            }
        })
    }

    private fun requestStatus(delay: Long) {
        val requestId = intent.getIntExtra(Int::class.java.canonicalName, 0)
        viewModel.getVideoModerationStatus(SdkHelper.sessionId, requestId, delay)
    }

    override fun onBackPressed() {
        showConfirmDialog(
            message = "Вы действительно хотите прервать видеозвонок?",
            okLabel = getString(R.string.yes),
            cancelLabel = getString(R.string.no),
            onOkClick = { finish() })
    }

    override fun onDestroy() {
        try {
            CookieSyncManager.createInstance(this)
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            wv_web.clearCache(true)
            wv_web.loadUrl("")
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        wv_web.apply {
            settings.apply {
                userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36"
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
            }
            webChromeClient = AppWebChromeClient()
            webViewClient = AppWebViewClient()
        }
    }

    class AppWebChromeClient : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                request.grant(request.resources)
            }
        }
    }

    class AppWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            view?.loadUrl(url)
            return true
        }
    }

    companion object {
        fun start(context: Context, link: String, requestId: Int) =
            context.startActivity<VideoCallActivity> {
                putExtra(String::class.java.canonicalName, link)
                putExtra(Int::class.java.canonicalName, requestId)
            }
    }

}