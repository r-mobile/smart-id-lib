package kg.onoi.smart_sdk.extensions

import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat

val String.Companion.empty: String
    get() = ""

fun String?.toFirstCapitalize() = this?.toLowerCase()?.capitalize()

fun String.removeWhitespaces() = this.replace(" ", "")

fun String.asMd5(): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    val digest = messageDigest.digest(this.toByteArray())
    return BigInteger(1, digest).toString(16)
}