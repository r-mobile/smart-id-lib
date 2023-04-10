package kg.onoi.smart_sdk.models

import android.hardware.Camera

data class CameraSize(val width: Int, val height: Int) {
    constructor(size: Camera.Size): this(size.width, size.height)
}