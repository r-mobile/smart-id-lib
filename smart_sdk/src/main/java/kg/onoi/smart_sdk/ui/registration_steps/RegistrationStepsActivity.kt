package kg.onoi.smart_sdk.ui.registration_steps

import android.Manifest
import android.os.Bundle
import com.tbruyelle.rxpermissions2.RxPermissions
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.setIsVisible
import kg.onoi.smart_sdk.extensions.showApplicationSettings
import kg.onoi.smart_sdk.models.RegistrationSteps
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.utils.NavigatorFacade
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_registration_steps.*

class RegistrationStepsActivity : SimpleActivity(R.layout.activity_registration_steps) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        cv_toolbar.init(this)
        btn_next.setOnClickListener { startFaceDetectionWithCheckPermission() }
        ll_face_scan.setIsVisible(SdkHelper.isContainStep(RegistrationSteps.USER_PHOTO))
        ll_passport_photo.setIsVisible(
            SdkHelper.isContainStep(RegistrationSteps.PASSPORT_FRONT)
                    || SdkHelper.isContainStep(RegistrationSteps.PASSPORT_BACK)
        )
        ll_selfie_with_passport.setIsVisible(SdkHelper.isContainStep(RegistrationSteps.PHOTO_WITH_PASSPORT))
    }

    private fun startFaceDetectionWithCheckPermission() {
        RxPermissions(this)
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe {
                when {
                    it.granted -> NavigatorFacade.startNextActivity(this)
                    it.shouldShowRequestPermissionRationale -> showRationaleMessage { showApplicationSettings() }
                    else -> showRationaleMessage { startFaceDetectionWithCheckPermission() }
                }
            }
    }

    private fun showRationaleMessage(action: () -> Unit) {
        showSimpleAlert(getString(R.string.take_permission_message)) {
            action()
        }
    }

    companion object {
        fun start(activity: BaseActivity<*>) {
            activity.startActivity(activity.createIntent(RegistrationStepsActivity::class))
        }
    }

}