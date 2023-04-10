package kg.onoi.smart_sdk.ui.photos

import android.os.Bundle
import androidx.lifecycle.Observer
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.models.NextStepEvents
import kg.onoi.smart_sdk.models.PhotoType
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.ui.passport_info.PassportInfoActivity
import kg.onoi.smart_sdk.ui.secret_word.registration.RegistrationSecretWordActivity
import kg.onoi.smart_sdk.utils.NavigatorFacade

class SelfieWithPassportPhotoActivity : BasePhotoActivity() {

    override fun onSuccessPhotoUploaded() = NavigatorFacade.startNextActivity(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.event.observe(this, Observer {
            when (it) {
                is NextStepEvents.ShowPassportInfoEvent -> PassportInfoActivity.start(this)
                is NextStepEvents.ShowSecretWordEvent -> RegistrationSecretWordActivity.start(this)
            }
        })
    }

    companion object {
        fun start(activity: SimpleActivity) {
            activity.startActivity(
                activity
                    .createIntent(SelfieWithPassportPhotoActivity::class)
                    .putExtra(Constant.Extra.PHOTO_TYPE, PhotoType.SELFIE_W_PASSPORT.name)
            )
        }

        fun startForResult(activity: BaseActivity<*>) {
            activity.startActivityForResult(
                activity.createIntent(SelfieWithPassportPhotoActivity::class)
                    .putExtra(Constant.Extra.PHOTO_TYPE, PhotoType.SELFIE_W_PASSPORT.name)
                    .putExtra(Constant.Extra.FOR_MODERATION, true), 0
            )
        }

    }

}