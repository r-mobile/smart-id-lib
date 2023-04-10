package kg.onoi.smart_sdk.ui.face_detection

import android.app.Activity
import android.os.Bundle
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.models.PhotoType
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.ui.photos.PhotoActivityVM
import kg.onoi.smart_sdk.utils.NavigatorFacade
import kg.onoi.smart_sdk.utils.SdkHelper

class RegistrationFaceDetectionActivity :
    FaceDetectionActivity<PhotoActivityVM>(PhotoActivityVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.user = user
        viewModel.setTargetPhotoType(PhotoType.SELFIE)
    }

    override fun onFaceRecognized(imagePath: String) {
        viewModel.uploadPhoto(imagePath)
    }

    override fun handleSuccessPhotoUploading() = if (isStartForModeration()) {
        setResult(Activity.RESULT_OK); finish()
    } else NavigatorFacade.startNextActivity(this)

    override fun switchCamera() {
        super.switchCamera()
        finish()
        when (intent.getBooleanExtra(Constant.Extra.FOR_MODERATION, false)) {
            true -> startForResult(callActivity!!)
            else -> start(this)
        }
    }

    companion object {

        private var callActivity: BaseActivity<*>? = null

        fun start(activity: SimpleActivity) {
            activity.startActivity(activity.createIntent(RegistrationFaceDetectionActivity::class))
        }

        fun startForResult(activity: BaseActivity<*>) {
            callActivity = activity
            activity.startActivityForResult(
                activity.createIntent(RegistrationFaceDetectionActivity::class)
                    .putExtra(Constant.Extra.FOR_MODERATION, true), 0
            )
        }
    }
}