package kg.onoi.smart_sdk.ui.face_detection

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
open class OverlayView : View {

    private val mTransparentPaint = Paint()

    private val mBorderColor = Paint()

    private val mPath = Path()

    var rect: RectF? = null

    var ovalColor: Int = Color.WHITE

    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(
        context,
        attr,
        defStyle
    )

    init {
        initPaints()
    }

    private fun initPaints() {
        mTransparentPaint.color = Color.TRANSPARENT
        mTransparentPaint.strokeWidth = 10f

        mBorderColor.strokeWidth = 10f
        mBorderColor.style = Paint.Style.STROKE
    }

    private fun getCaptureRegionForScreen(): Rect {
        val metrics = context.resources.displayMetrics
        val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        val coef = if (ratio > 1.7) 4.4 else 5.0

        val left = width / 6
        val top = (height.toFloat() / coef).toInt()
        val right = width - left
        val bottom = height - top

        return Rect(left, top, right, bottom)
    }

    var canvas: Canvas? =null
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas =canvas
        mBorderColor.color = ovalColor
        if (rect == null) {
            rect = RectF(getCaptureRegionForScreen())
        }

        mPath.reset()

        mPath.addOval(rect, Path.Direction.CW)
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD

        canvas.drawOval(rect!!, mTransparentPaint)
        canvas.drawOval(rect!!, mBorderColor)

        canvas.drawPath(mPath, mTransparentPaint)
        canvas.clipPath(mPath)
        canvas.drawColor(Color.parseColor("#AA000000"))

    }

    fun updateColor(i: Int) {
        ovalColor = i
    }
}