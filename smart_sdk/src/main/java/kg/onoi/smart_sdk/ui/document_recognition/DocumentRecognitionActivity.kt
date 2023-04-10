package kg.onoi.smart_sdk.ui.document_recognition

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import biz.smartengines.smartid.swig.RecognitionResult
import kg.onoi.smart_sdk.extensions.showConfirmDialog
import kg.onoi.smart_sdk.extensions.showSimpleAlert
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.models.Constant
import kg.onoi.smart_sdk.recognition.ResultRecognitionWrapper
import kg.onoi.smart_sdk.recognition.SmartIDView
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kotlinx.android.synthetic.main.activity_document_recognition.*
import kotlinx.android.synthetic.main.activity_document_recognition.cv_toolbar
import org.parceler.Parcels


class DocumentRecognitionActivity :
    SimpleActivity(R.layout.activity_document_recognition),
    SmartIDView.Callback {

    private val smartIdView: SmartIDView by lazy { SmartIDView() }
    private val targetSide: ResultRecognitionWrapper.DocumentSide by lazy { getTargetDocumentSide() }
    private var recognitionResult: ResultRecognitionWrapper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRecognitionEngine()
        initViews()
    }

    private fun initRecognitionEngine() {
        smartIdView.initializeEngine(this, this)
        smartIdView.setSurface(documentView, rl_draw)
    }

    private fun initViews() {
        if (isStartForModeration()) cv_toolbar.init(this, R.string.passport) { finish() }
        else cv_toolbar.init(this, R.string.passport) { onBackPressed() }
        tv_title.text = when (getTargetDocumentSide()) {
            ResultRecognitionWrapper.DocumentSide.FRONT -> getString(R.string.focus_on_passport_front_side)
            ResultRecognitionWrapper.DocumentSide.BACK -> getString(R.string.focus_on_passport_back_side)
        }
        tv_title.postDelayed({ startDocumentRecognition() }, 500)
    }

    private fun startDocumentRecognition() {
        smartIdView.startRecognition("*", "15.0")
    }

    override fun onDestroy() {
        smartIdView.stopRecognition()
        super.onDestroy()
    }

    override fun onBackPressed() = finish()

    private fun getTargetDocumentSide(): ResultRecognitionWrapper.DocumentSide {
        return ResultRecognitionWrapper.DocumentSide.valueOf(
            intent.getStringExtra(ResultRecognitionWrapper.DocumentSide::class.java.canonicalName)
        )
    }

    private fun handleDocumentRecognized(result: ResultRecognitionWrapper) = when {
        result.isUnrecognized() -> showRepeatDialog(
            getString(R.string.document_not_recognized),
            ::startDocumentRecognition
        )
        targetSide != result.getDocumentSide() -> showRepeatDialog(
            getString(R.string.invalid_document_side),
            ::startDocumentRecognition
        )
        result.isValidDocType() -> {
            recognitionResult = result
            smartIdView.takePicture(recognitionResult)
        }
        else -> showRepeatDialog(
            getString(R.string.invalid_document_type),
            ::startDocumentRecognition
        )
    }

    private fun showRepeatDialog(message: String, positiveBtnCallback: () -> Unit = { finish() }) {
        showConfirmDialog(null,
            message,
            getString(R.string.repeat),
            getString(R.string.close),
            positiveBtnCallback,
            { finish() })
    }


    //region SmartIDCallback
    override fun initialized(engine_initialized: Boolean) {
        when (engine_initialized) {
            false -> showSimpleAlert(getString(R.string.recognition_not_initialized)) { finish() }
        }
    }

    override fun recognized(result: RecognitionResult?) {
        result?.let {
            val wrapper = ResultRecognitionWrapper()
            wrapper.init(it)
            if (wrapper.isSuccessRecognized()) {
                smartIdView.stopRecognition()
                handleDocumentRecognized(wrapper)
            } else {
                if (it.IsTerminal()) {
                    smartIdView.stopRecognition()
                    showRepeatDialog(
                        getString(R.string.document_not_recognized),
                        ::startDocumentRecognition
                    )
                }
            }
        }
    }

    override fun error(message: String?) {
        try {
            showSimpleAlert(
                message ?: getString(kg.onoi.smart_sdk.R.string.unknown_error)
            ) { finish() }
        } catch (_: Exception) {
        }
    }

    override fun photoCreated(path: String?) {
        path?.let {
            recognitionResult?.picturePath = it
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(Constant.Extra.REC_INFO, Parcels.wrap(recognitionResult))
            )
        }
        finish()
    }
    //endregion


    companion object {

        const val REQUEST_CODE = 3

        fun startForResult(
            activity: Activity,
            side: ResultRecognitionWrapper.DocumentSide,
            forModeration: Boolean = false
        ) {
            val intent = Intent(activity, DocumentRecognitionActivity::class.java).apply {
                putExtra(ResultRecognitionWrapper.DocumentSide::class.java.canonicalName, side.name)
                putExtra(Constant.Extra.FOR_MODERATION, forModeration)
            }
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }
}