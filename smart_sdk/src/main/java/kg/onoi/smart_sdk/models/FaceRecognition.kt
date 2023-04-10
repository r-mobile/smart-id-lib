package kg.onoi.smart_sdk.models

import android.graphics.Rect
import android.hardware.Camera
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import kg.onoi.smart_sdk.utils.SdkHelper
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min


class FaceRecognition(
    private val surface: SurfaceView,
    val size: Pair<Int, Int>,
    var cameraType: Int = CameraSource.CAMERA_FACING_FRONT) {

    private lateinit var cameraSource: CameraSource
    private lateinit var faceTrackerFactory: FaceTrackerFactory


    init {
        createCameraSource()
        setupSurface()
    }

    fun getRecognitionEventObserver(): Flowable<FaceRecognitionEvents> =
        faceTrackerFactory.recognitionEventObserver

    private fun setupSurface() {
        surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                stopCamera()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                startCamera()
            }

        })
    }

    fun startCamera(): CameraSource = cameraSource.start(surface.holder)

    fun stopCamera() {
        try {
            cameraSource.stop()
            cameraSource.release()
        } catch (_: Exception) {
        }
    }

    fun savePhoto(callback: (String) -> Unit) {
        faceTrackerFactory.lockRecognitionProcess()
        val createImageFile = createImageFile()
        val outStream = FileOutputStream(createImageFile)
        cameraSource.takePicture(null, CameraSource.PictureCallback {
            try {
                outStream.write(it)
                outStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                outStream.close()
                callback(createImageFile.absolutePath)
            }
        })
    }

    private fun createImageFile(): File {
        val storageDir: File? =
            surface.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("selfie", ".jpg", storageDir).absoluteFile
    }

    private fun createCameraSource() {
        faceTrackerFactory = FaceTrackerFactory()

        val actualCameraSize = getCameraSize()

        val min = min(actualCameraSize.width.toFloat(), actualCameraSize.height.toFloat())
        val max = max(actualCameraSize.width.toFloat(), actualCameraSize.height.toFloat())
        faceTrackerFactory.x = size.first.toFloat() / min
        faceTrackerFactory.y = size.second.toFloat() / max

        faceTrackerFactory.width = size.first.toFloat()
        faceTrackerFactory.height = size.second.toFloat()

        val faceDetector = createFaceDetector(faceTrackerFactory)

        cameraSource = CameraSource.Builder(surface.context, faceDetector)
            .setAutoFocusEnabled(true)
            .setRequestedPreviewSize(actualCameraSize.width, actualCameraSize.height)
            .setFacing(cameraType)
            .setRequestedFps(30f)
            .build()

    }

    var camera: Camera? = null

    private fun getCameraSize(): CameraSize {
        var validSize: CameraSize? = null
        val numCameras: Int = Camera.getNumberOfCameras()
        for (i in 0 until numCameras) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == cameraType) {
                camera = Camera.open(i)
                val cameraParams: Camera.Parameters = camera!!.parameters
                val sizes: List<Camera.Size> = cameraParams.supportedPreviewSizes
                getOptimalPreviewSize(sizes, size.first.toFloat(), size.second.toFloat())?.let {
                    validSize = CameraSize(it)
                    cameraParams.setPreviewSize(validSize!!.width, validSize!!.height)
                }
                camera?.release()
            }
        }
        return validSize ?: CameraSize(size.first, size.second)
    }

    private fun getOptimalPreviewSize(
        sizes: List<Camera.Size>?,
        w: Float,
        h: Float
    ): Camera.Size? {
        val ASPECT_TH = 0.1F
        var minDiff = Double.MAX_VALUE

        val targetHeight = h.toInt()
        var ratio: Double
        var optimalSize: Camera.Size? = null

        val targetRatio = h.toDouble() / w.toDouble()

        for (s in sizes!!) {
            ratio = s.width.toDouble() / s.height.toDouble()
            if (Math.abs(ratio - targetRatio) <= ASPECT_TH) {
                if (Math.abs(targetHeight - s.height) < minDiff) {
                    optimalSize = s
                    minDiff = Math.abs(targetHeight - s.height).toDouble()
                }
            }
        }

        return optimalSize
    }

    private fun createFaceDetector(trackerFactory: MultiProcessor.Factory<Face>): FaceDetector? {
        val detector = FaceDetector.Builder(surface.context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build()
        detector.setProcessor(MultiProcessor.Builder<Face>(trackerFactory).build())
        return detector
    }

    fun unlockRecognitionProcess() = faceTrackerFactory.unlockRecognitionProcess()

}

class FaceTrackerFactory : MultiProcessor.Factory<Face> {

    var x: Float = 1F
    var y: Float = 1F

    var width: Float = 0F
    var height: Float = 0F

    private var smilingProbability: Float = 0.95F
    private var isRecognitionLockNow = false
    private var source: ObservableRecognitionEvent = ObservableRecognitionEvent()
    var recognitionEventObserver: Flowable<FaceRecognitionEvents> = Flowable
        .create<FaceRecognitionEvents>(source, BackpressureStrategy.DROP)
        .timeout(1, TimeUnit.SECONDS, Flowable.just(FaceRecognitionEvents.NoFacesEvent()))
        .repeat()

    override fun create(p0: Face?): Tracker<Face> {
        return object : Tracker<Face>() {
            override fun onUpdate(p0: Detector.Detections<Face>?, face: Face?) {
                super.onUpdate(p0, face)
                if (isRecognitionLockNow) return
                val faceCount = p0?.detectedItems?.size() ?: 0
                source.emit(
                    when {
                        faceCount <= 0 -> FaceRecognitionEvents.NoFacesEvent()
                        faceCount > 1 -> FaceRecognitionEvents.ManyFacesEvent()
                        isFaceWithoutSmile(face!!) -> {
                            FaceRecognitionEvents.FaceEvent(
                                getFaceRect(face),
                                isOpenEyes(face)
                            )
                        }
                        isFaceWithSmile(face) -> {
                            FaceRecognitionEvents.SmilingFaceEvent(
                                getFaceRect(face),
                                isOpenEyes(face)
                            )
                        }
                        else -> FaceRecognitionEvents.NoFacesEvent()
                    }
                )
            }
        }
    }

    private fun isOpenEyes(face: Face) = face.isLeftEyeOpenProbability > 0.08
            && face.isRightEyeOpenProbability > 0.08

    private fun isFaceWithSmile(face: Face) =
        face.isSmilingProbability >= smilingProbability

    private fun isFaceWithoutSmile(face: Face) =
        face.isSmilingProbability < smilingProbability

    private fun getFaceRect(face: Face): Rect {
        val positionCoefficient = 2.5F
        val xCoefficient = scaleX(face.width / positionCoefficient)
        val yCoefficient = scaleY(face.height / positionCoefficient)

        val a = translateX(face.position.x + face.width / 2)
        val b = translateY(face.position.y + face.height / 2)

        val left = a - xCoefficient
        val top = b + yCoefficient
        val right = a + xCoefficient
        val bottom = b - yCoefficient

        return Rect(left.toInt(), bottom.toInt(), right.toInt(), top.toInt())
    }

    fun lockRecognitionProcess() {
        isRecognitionLockNow = true
    }

    fun unlockRecognitionProcess() {
        isRecognitionLockNow = false
    }

    fun scaleX(horizontal: Float) = horizontal * y

    fun scaleY(vertical: Float) = vertical * x

    fun translateX(x: Float): Float = width - scaleX(x)

    fun translateY(y: Float): Float = scaleY(y)

}