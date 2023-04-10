package kg.onoi.smart_sdk.network

import com.google.gson.annotations.SerializedName

data class Response<T>(
    val result: T,
    val status: Status,
    val message: String)

enum class Status {
    @SerializedName("success")
    SUCCESS,
    @SerializedName("fail")
    FAIL
}