package kg.onoi.smart_sdk.utils

import kg.onoi.smart_sdk.models.RegistrationSteps
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kg.onoi.smart_sdk.ui.face_detection.RegistrationFaceDetectionActivity
import kg.onoi.smart_sdk.ui.passport_info.PassportInfoActivity
import kg.onoi.smart_sdk.ui.photos.PassportBackPhotoActivity
import kg.onoi.smart_sdk.ui.photos.PassportFrontPhotoActivity
import kg.onoi.smart_sdk.ui.photos.SelfieWithPassportPhotoActivity
import kg.onoi.smart_sdk.ui.registration_steps.RegistrationStepsActivity
import kg.onoi.smart_sdk.ui.secret_word.registration.RegistrationSecretWordActivity

object NavigatorFacade {
    fun startNextActivity(current: SimpleActivity) {
        when (current::class) {
            RegistrationStepsActivity::class -> fromRegistrationStepsActivity(current)
            RegistrationFaceDetectionActivity::class -> fromRegistrationFaceDetectionActivity(
                current
            )
            PassportFrontPhotoActivity::class -> fromPassportFrontPhotoActivity(current)
            PassportBackPhotoActivity::class -> fromPassportBackPhotoActivity(current)
            SelfieWithPassportPhotoActivity::class -> fromSelfieWithPassportPhotoActivity(current)
            else -> PassportInfoActivity.start(current)
        }
    }

    private fun fromRegistrationStepsActivity(activity: SimpleActivity) {
        when {
            SdkHelper.isContainStep(RegistrationSteps.USER_PHOTO) -> {
                RegistrationFaceDetectionActivity.start(activity)
            }
            else -> fromRegistrationFaceDetectionActivity(activity)
        }
    }

    private fun fromRegistrationFaceDetectionActivity(activity: SimpleActivity) {
        when {
            SdkHelper.isContainStep(RegistrationSteps.PASSPORT_FRONT) -> {
                PassportFrontPhotoActivity.start(activity)
            }
            else -> fromPassportFrontPhotoActivity(activity)
        }
    }

    private fun fromPassportFrontPhotoActivity(activity: SimpleActivity) {
        when {
            SdkHelper.isContainStep(RegistrationSteps.PASSPORT_BACK) ->{
                PassportBackPhotoActivity.start(activity)
            }
            else -> fromPassportBackPhotoActivity(activity)
        }
    }

    private fun fromPassportBackPhotoActivity(activity: SimpleActivity) {
        when {
            SdkHelper.isContainStep(RegistrationSteps.PHOTO_WITH_PASSPORT) -> {
                SelfieWithPassportPhotoActivity.start(activity)
            }
            else -> fromSelfieWithPassportPhotoActivity(activity)
        }
    }

    private fun fromSelfieWithPassportPhotoActivity(activity: SimpleActivity) {
        when {
            SdkHelper.isContainStep(RegistrationSteps.SECRET_WORD) -> RegistrationSecretWordActivity.start(
                activity
            )
            else -> PassportInfoActivity.start(activity)
        }
    }


}