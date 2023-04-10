package kg.onoi.smart_sdk.ui.face_detection

import android.Manifest
import androidx.lifecycle.Observer
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.google.android.gms.vision.CameraSource
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.extensions.setIsVisible
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kg.onoi.smart_sdk.utils.SdkHelper
import kotlinx.android.synthetic.main.activity_reg_face_detection.*
import kotlin.reflect.KClass


abstract class FaceDetectionActivity<VM : BaseViewModel>(vmClass: KClass<VM>) : BaseActivity<VM>(
    R.layout.activity_reg_face_detection,
    vmClass
) {

    private var faceRecognition: FaceRecognition? = null
    private var smileCounter = 0
    private var disposable: Disposable? = null

    private var openEyesCounter: Int = 0
    private var lastEyesState: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        subscribeToLiveData()
    }

    private fun startFaceRecognition() {
        val permissions = RxPermissions(this)
        if (permissions.isGranted(Manifest.permission.CAMERA)
            && permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            runFaceRecognitionView()
        } else runWithCheckPermission(permissions)
    }

    override fun onResume() {
        super.onResume()
        if (faceRecognition == null) startFaceRecognition()
    }

    override fun onStop() {
        disposable?.dispose()
        faceRecognition?.stopCamera()
        super.onStop()
    }

    override fun onBackPressed() {
        if (isStartForModeration()) finish()
        else super.onBackPressed()
    }

    private fun runWithCheckPermission(permissions: RxPermissions) {
        permissions
            .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { granted ->
                if (granted) {
                    runFaceRecognitionView();faceRecognition?.startCamera()
                } else showSimpleAlert(getString(R.string.take_permission_message)) {
                    runWithCheckPermission(
                        permissions
                    )
                }

            }
    }

    private fun runFaceRecognitionView() {
        surface.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                surface.viewTreeObserver.removeOnGlobalLayoutListener(this)
                faceRecognition = FaceRecognition(
                    surface,
                    Pair(surface.width, surface.height),
                    SdkHelper.cameraType
                )
                disposable = faceRecognition
                    ?.getRecognitionEventObserver()
                    ?.subscribeOn(Schedulers.single())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe {
                        when (it) {
                            is FaceRecognitionEvents.FaceEvent -> handleFaceDetected(
                                it.rect,
                                it.eyesIsOpen
                            )
                            is FaceRecognitionEvents.SmilingFaceEvent -> handleFaceDetected(
                                it.rect,
                                it.eyesIsOpen,
                                true
                            )
                            is FaceRecognitionEvents.ManyFacesEvent -> errorRecognitionState(
                                getString(
                                    R.string.many_faces_error
                                )
                            )
                            is FaceRecognitionEvents.UnexpectedSmilingFaceEvent -> errorRecognitionState(
                                getString(R.string.dont_smile)
                            )
                            is FaceRecognitionEvents.NoFacesEvent -> normalRecognitionState(
                                getString(
                                    R.string.put_face_on_center_photo
                                )
                            )
                        }
                    }
            }
        })
    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            when (it) {
                is Event.Success -> handleSuccessPhotoUploading()
                is Event.Fail -> resetRecognitionState()
            }
        })
    }

    private fun setupViews() {
        if (isStartForModeration()) cv_toolbar.init(this, R.string.face_scaning_title) { finish() }
        else cv_toolbar.init(this, R.string.face_scaning_title)

        btn_switch.apply {
            setOnClickListener { switchCamera() }
            isVisible = SdkHelper.settings.useMainCamera
        }

        iv_manual_shot.setOnClickListener { faceRecognition?.savePhoto { onFaceRecognized(it) } }
    }

    private fun handleFaceDetected(
        rect: Rect,
        withOpenEyes: Boolean = true,
        withSmile: Boolean = false
    ) {
        calculateEyeBlinking(withOpenEyes)

        val isFaceInOval = oval.rect?.contains(RectF(rect)) ?: false
        if (!isFaceInOval) {
            smileCounter = 0
            openEyesCounter = 0
            lastEyesState = true
            normalRecognitionState(getString(R.string.put_face_to_oval))
            return
        }

        if (SdkHelper.settings.checkEyes && openEyesCounter < 1) {
            successRecognitionState(getString(R.string.look_at_camera))
            return
        }

        if (!SdkHelper.settings.checkSmile) {
            successRecognitionState(getString(R.string.look_at_camera))
            iv_manual_shot.setIsVisible(withOpenEyes)
            return
        }

        if (withSmile) {
            when (smileCounter < 5) {
                true -> errorRecognitionState(getString(R.string.make_serious_face))
                false -> {
                    successRecognitionState(getString(R.string.look_at_camera))
                    faceRecognition?.savePhoto { onFaceRecognized(it) }
                }
            }
        } else {
            smileCounter++
            successRecognitionState(getString(R.string.smile_please))
        }
    }

    private fun calculateEyeBlinking(withOpenedEyes: Boolean) {
        if (lastEyesState && !withOpenedEyes) openEyesCounter = openEyesCounter?.plus(1)
        lastEyesState = withOpenedEyes
    }

    private fun updateOvalColor(color: Int) {
        oval.updateColor(color)
        oval.invalidate()
    }

    private fun errorRecognitionState(text: String) {
        smileCounter = 0
        updateOvalColor(Color.RED)
        tv_message.text = text
        hideManualShotBtn()
    }

    private fun normalRecognitionState(text: String) {
        smileCounter = 0
        updateOvalColor(Color.WHITE)
        tv_message.text = text
        hideManualShotBtn()
    }

    private fun successRecognitionState(text: String) {
        updateOvalColor(Color.GREEN)
        tv_message.text = text
    }

    private fun resetRecognitionState() {
        faceRecognition?.unlockRecognitionProcess()
    }

    fun hideManualShotBtn() = iv_manual_shot.setIsVisible(false)

    abstract fun onFaceRecognized(imagePath: String)

    abstract fun handleSuccessPhotoUploading()

    open fun switchCamera() {
        faceRecognition?.stopCamera()
        SdkHelper.cameraType = when (SdkHelper.cameraType) {
            CameraSource.CAMERA_FACING_BACK -> CameraSource.CAMERA_FACING_FRONT
            else -> CameraSource.CAMERA_FACING_BACK
        }
    }
}

