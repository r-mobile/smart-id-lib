package kg.onoi.smart_sdk.ui.photos

import android.Manifest
import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import androidx.core.content.ContextCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.extensions.showToast
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.PhotoType
import kg.onoi.smart_sdk.extensions.gone
import kg.onoi.smart_sdk.extensions.invisible
import kg.onoi.smart_sdk.extensions.visible
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.models.Event
import kg.onoi.smart_sdk.recognition.ResultRecognitionWrapper
import kg.onoi.smart_sdk.ui.base.BaseActivity
import kg.onoi.smart_sdk.ui.document_recognition.DocumentRecognitionActivity
import kg.onoi.smart_sdk.ui.internal_camera.InternalCameraActivity
import kg.onoi.smart_sdk.utils.Utils
import kotlinx.android.synthetic.main.activity_photo_new.*
import org.parceler.Parcels
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


abstract class BasePhotoActivity :
    BaseActivity<PhotoActivityVM>(
        R.layout.activity_photo_new,
        PhotoActivityVM::class
    ) {

    private var resizedPhoto: Bitmap? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.user = user
        viewModel.setTargetPhotoType(PhotoType.valueOf(intent.getStringExtra(Constant.Extra.PHOTO_TYPE)))
        setupViews()
        subscribeToLiveData()
    }

    private fun setupViews() {
        tv_photo_desc.text = getString(viewModel.getPrePhotoDescription())
        when (viewModel.photoType) {
            PhotoType.SELFIE, PhotoType.SELFIE_W_PASSPORT -> setupForCreateSelfiePhoto(viewModel.photoType)
            else -> setupForCreateDocumentPhoto()
        }

    }

    private fun subscribeToLiveData() {
        viewModel.event.observe(this, Observer {
            it?.let {
                when (it) {
                    is Event.Success -> {
                        if (isStartForModeration()) {
                            setResult(Activity.RESULT_OK); finish()
                        } else onSuccessPhotoUploaded()
                    }
                }
            }
        })
    }

    private fun setupForCreateDocumentPhoto() {
        if (isStartForModeration()) cv_toolbar.init(this, R.string.passport) { finish() }
        else cv_toolbar.init(this, R.string.passport) { onBackPressed() }
        iv_photo.invisible()
        pcv_passport_placeholder.apply {
            setImageDrawable(getDrawableByType(viewModel.photoType))
            visible()
        }
        ll_create_photo.visible()
        ll_recreate_photo.gone()

        btn_recreate.text = getString(R.string.scan_again)
        btn_create.setText(R.string.scan)
        btn_create.setOnClickListener { createPhoto() }
    }

    override fun onBackPressed() {
        if (isStartForModeration()) finish()
        else super.onBackPressed()
    }

    private fun getDrawableByType(photoType: PhotoType): Drawable? {
        val imgRes = when (photoType) {
            PhotoType.PASSPORT_FRONT -> R.drawable.passport_mock_front
            else -> R.drawable.passport_mock_back
        }
        return ContextCompat.getDrawable(this, imgRes)
    }


    private fun setupForCreateSelfiePhoto(photoType: PhotoType) {
        val title: Int
        val placeHolder: Int

        if (photoType == PhotoType.SELFIE_W_PASSPORT) {
            title = kg.onoi.smart_sdk.R.string.selfie_with_passport_photo_title
            placeHolder = viewModel.getSelfieWithPassportPlaceholder()
        } else {
            title = kg.onoi.smart_sdk.R.string.selfie
            placeHolder = viewModel.getSelfiePlaceholder()
        }

        cv_toolbar.init(this, title)

        pcv_passport_placeholder.invisible()

        iv_photo.visible()
        iv_photo.setImageResource(placeHolder)

        ll_create_photo.visible()
        ll_recreate_photo.gone()

        btn_recreate.text = getString(R.string.rephoto)
        btn_create.text = getString(R.string.create_photo)
        btn_create.setOnClickListener { createPhoto() }
    }


    private fun setupForPreview() {
        tv_photo_desc.text = getString(viewModel.getPostPhotoDescription())
        iv_photo.apply {
            setImageBitmap(resizedPhoto)
            visible()
        }
        pcv_passport_placeholder.invisible()
        ll_create_photo.invisible()
        ll_recreate_photo.visible()

        btn_recreate.setOnClickListener { createPhoto() }
        btn_next.setOnClickListener { viewModel.uploadPhoto(currentPhotoPath) }
    }

    abstract fun onSuccessPhotoUploaded()

    private fun createPhoto() {
        viewModel.resetRecognitionInfo()
        RxPermissions(this)
            .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(
                { granted ->
                    if (granted) {
                        when (viewModel.photoType) {
                            PhotoType.PASSPORT_BACK, PhotoType.PASSPORT_FRONT -> runRecognitionActivity()
                            else -> runCameraApplication()
                        }
                    }
                },
                { showSimpleAlert(getString(kg.onoi.smart_sdk.R.string.missing_permission_message)) })
    }

    private fun runRecognitionActivity() {
        val side = when (viewModel.photoType == PhotoType.PASSPORT_FRONT) {
            true -> ResultRecognitionWrapper.DocumentSide.FRONT
            else -> ResultRecognitionWrapper.DocumentSide.BACK
        }
        DocumentRecognitionActivity.startForResult(this, side, isStartForModeration())
    }

    private fun runCameraApplication() {
        try {
            createImageFile()
        } catch (ex: IOException) {
            showToast(ex.message ?: getString(R.string.unknown_error))
            null
        }?.also {
            InternalCameraActivity.startForResult(this, REQUEST_IMAGE_CAPTURE, it.absolutePath)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    updateImageState()
                }

                DocumentRecognitionActivity.REQUEST_CODE -> {
                    val resultRecognition = Parcels.unwrap<ResultRecognitionWrapper>(
                        data?.getParcelableExtra(Constant.Extra.REC_INFO)
                    )
                    if (viewModel.checkIsOneDocument(resultRecognition)) {
                        currentPhotoPath = resultRecognition.picturePath
                        viewModel.saveRecognitionResult(resultRecognition)
                        updateImageState()
                    } else {
                        showSimpleAlert(getString(R.string.different_documents_error)) {
                            PassportFrontPhotoActivity.start(this)
                            finish()
                        }
                    }
                }
            }

        }
    }

    private fun updateImageState() {
        val decodeFile = BitmapFactory.decodeFile(currentPhotoPath)
        decodeFile?.let {
            compressAndSave(it)
            setupForPreview()
        }
    }

    private fun compressAndSave(it: Bitmap) {
        try {
            resizedPhoto = Utils.getResizedBitmap(it, 1024)
            resizedPhoto = Utils.correctOrientation(currentPhotoPath)
            val fos = FileOutputStream(File(currentPhotoPath))
            resizedPhoto?.compress(Bitmap.CompressFormat.JPEG, 50, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            showToast(e.message ?: getString(R.string.unknown_error))
        }
    }

    private fun createImageFile(): File? {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(viewModel.getDocType().name, ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}