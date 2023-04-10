package kg.onoi.smart_sdk.ui.photos

import android.app.Activity
import android.content.Context
import android.content.Intent
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.models.PhotoType
import kg.onoi.smart_sdk.ui.passport_info.PassportInfoActivity

class SelfiePhotoActivity : BasePhotoActivity() {

    override fun onSuccessPhotoUploaded() = PassportInfoActivity.start(this)

    companion object {
        fun start(context: Context, sessionId: String) =
            context.startActivity(
                Intent(context, SelfiePhotoActivity::class.java).apply {
                    putExtra(Constant.Extra.PHOTO_TYPE, PhotoType.SELFIE.name)
                    putExtra(Constant.Extra.SESSION_ID, sessionId)
                }
            )

        fun startForResult(activity: Activity, sessionId: String) {
            activity.startActivityForResult(
                Intent(activity, SelfieWithPassportPhotoActivity::class.java).apply {
                    putExtra(Constant.Extra.PHOTO_TYPE, PhotoType.SELFIE.name)
                    putExtra(Constant.Extra.SESSION_ID, sessionId)
                    putExtra(Constant.Extra.FOR_MODERATION, true) }, 0)
        }

    }

}