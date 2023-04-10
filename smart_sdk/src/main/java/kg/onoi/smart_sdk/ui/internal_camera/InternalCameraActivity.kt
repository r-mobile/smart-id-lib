package kg.onoi.smart_sdk.ui.internal_camera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.fotoapparat.Fotoapparat
import io.fotoapparat.result.WhenDoneListener
import io.fotoapparat.selector.front
import kg.onoi.smart_sdk.R
import kg.onoi.smart_sdk.ui.base.SimpleActivity
import kotlinx.android.synthetic.main.activity_internal_camera.*
import java.io.File

class InternalCameraActivity : SimpleActivity(R.layout.activity_internal_camera) {

    private val fotoapparat by lazy {
        Fotoapparat(
            context = this,
            view = camera_view,

            lensPosition = front()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        cv_toolbar.init(this, R.string.photo) { finish() }
        btn_create_photo.setOnClickListener { takePhoto() }
    }

    private fun takePhoto() {
        val file = File(intent.getStringExtra(String::class.java.canonicalName))
        fotoapparat
            .takePicture()
            .saveToFile(file)
            .whenDone(object : WhenDoneListener<Unit> {
                override fun whenDone(it: Unit?) {
                    finishWithResult()
                }
            })
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onStart() {
        super.onStart()

        fotoapparat.start()
    }

    override fun onStop() {
        fotoapparat.stop()
        super.onStop()
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        fun startForResult(activity: AppCompatActivity, code: Int, fileName: String) {
            activity.startActivityForResult(
                Intent(activity, InternalCameraActivity::class.java).apply {
                    putExtra(String::class.java.canonicalName, fileName)
                }, code
            )
        }
    }
}