package kg.onoi.smart_sdk.models

import android.graphics.Rect
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe

open class ObservableRecognitionEvent : FlowableOnSubscribe<FaceRecognitionEvents> {
    var emitter: FlowableEmitter<FaceRecognitionEvents>? = null

    override fun subscribe(emitter: FlowableEmitter<FaceRecognitionEvents>) {
        this.emitter = emitter
    }

    fun emit(face: FaceRecognitionEvents?) {
        emitter?.onNext(face ?: FaceRecognitionEvents.NoFacesEvent())
    }
}

sealed class FaceRecognitionEvents {
    class NoFacesEvent : FaceRecognitionEvents()
    class ManyFacesEvent : FaceRecognitionEvents()
    class FaceEvent(val rect: Rect, val eyesIsOpen: Boolean) : FaceRecognitionEvents()
    class UnexpectedSmilingFaceEvent(val rect: Rect, val eyesIsOpen: Boolean) : FaceRecognitionEvents()
    class SmilingFaceEvent(val rect: Rect, val eyesIsOpen: Boolean) : FaceRecognitionEvents()
}