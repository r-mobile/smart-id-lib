package kg.onoi.smart_sdk.ui.face_detection

import android.content.Context
import android.content.Intent
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.showConfirmDialog
import kg.onoi.smart_sdk.ui.face_detection.vm.AuthFaceDetectionVM
import kg.onoi.smart_sdk.ui.face_detection.vm.CaptureVM
import kg.onoi.smart_sdk.ui.secret_word.BaseSecretWordActivity
import kg.onoi.smart_sdk.ui.secret_word.auth.AuthSecretWordActivity
import kg.onoi.smart_sdk.utils.SdkHelper

class FaceCapturingActivity :
    FaceDetectionActivity<CaptureVM>(CaptureVM::class) {
    override fun onFaceRecognized(imagePath: String) {
        finish()
        capturingCallback(imagePath)
    }

    override fun handleSuccessPhotoUploading() {}

    override fun onBackPressed() { finish() }

    companion object {
        var capturingCallback: (String) -> Unit = {}
        fun start(context: Context, sessionId: String, callback: (String) -> Unit) {
            SdkHelper.sessionId = sessionId
            capturingCallback = callback
            context.startActivity(Intent(context, FaceCapturingActivity::class.java))
        }
    }
}