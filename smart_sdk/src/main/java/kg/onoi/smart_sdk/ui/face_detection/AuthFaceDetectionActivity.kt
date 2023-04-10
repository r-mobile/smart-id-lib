package kg.onoi.smart_sdk.ui.face_detection

import android.content.Context
import android.content.Intent
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.showConfirmDialog
import kg.onoi.smart_sdk.ui.face_detection.vm.AuthFaceDetectionVM
import kg.onoi.smart_sdk.ui.secret_word.auth.AuthSecretWordActivity
import kg.onoi.smart_sdk.utils.ResponseWay
import kg.onoi.smart_sdk.utils.SdkHelper

class AuthFaceDetectionActivity :
    FaceDetectionActivity<AuthFaceDetectionVM>(AuthFaceDetectionVM::class) {
    override fun onFaceRecognized(imagePath: String) {
        viewModel.uploadAuthPhoto(imagePath)
    }

    override fun handleSuccessPhotoUploading() {
        if (isRunForResult()) {
            finish(); SdkHelper.faceRecognitionCallback.invoke(true)
        } else {
            when (intent.getBooleanExtra(Boolean::class.java.canonicalName, false)) {
                true -> AuthSecretWordActivity.start(this)
                else -> {
                    finishWithOkResult()
                    SdkHelper.actionCallback.invoke(SdkHelper.sessionId, ResponseWay.AUTH)
                }
            }
        }
    }

    private fun isRunForResult() = intent.getBooleanExtra(FOR_RESULT, false)

    override fun onBackPressed() {
        showCloseDialog()
    }

    private fun showCloseDialog() {
        val pair: Pair<String, () -> Unit> = when (isRunForResult()) {
            true -> Pair(getString(R.string.cancel_action_query),
                { SdkHelper.faceRecognitionCallback.invoke(false); closeActivityStack() })
            else -> Pair(getString(R.string.cancel_auth_query), { closeActivityStack() })
        }
        showConfirmDialog(null,
            pair.first,
            getString(R.string.yes),
            getString(R.string.no),
            { pair.second() })
    }

    override fun switchCamera() {
        super.switchCamera()
        finish()
        when (isRunForResult()) {
            true -> start(this, SdkHelper.sessionId, SdkHelper.faceRecognitionCallback)
            else -> start(this, intent.getBooleanExtra(Boolean::class.java.canonicalName, false))
        }
    }


    companion object {
        private const val FOR_RESULT = "FOR_RESULT"

        fun start(context: Context, needSecretWord: Boolean = false) {
            context.startActivity(Intent(context, AuthFaceDetectionActivity::class.java).apply {
                putExtra(Boolean::class.java.canonicalName, needSecretWord)
            })
        }

        fun start(context: Context, sessionId: String, callback: (Boolean) -> Unit) {
            SdkHelper.sessionId = sessionId
            SdkHelper.faceRecognitionCallback = callback
            context.startActivity(
                Intent(context, AuthFaceDetectionActivity::class.java).apply {
                    putExtra(FOR_RESULT, true)
                })
        }
    }
}