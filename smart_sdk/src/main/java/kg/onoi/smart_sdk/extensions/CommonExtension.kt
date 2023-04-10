package kg.onoi.smart_sdk.extensions

import android.content.res.Resources
import android.util.DisplayMetrics

fun Resources.convertPixelsToDp(px: Float): Float {
    return (px / (this.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun Resources.convertDpToPixels(dp: Float): Float {
    return (dp * (this.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun <K, V> MutableMap<K, V>.getOrNull(key: K): V? = try {
    getValue(key)
} catch (e: Exception) {
    null
}