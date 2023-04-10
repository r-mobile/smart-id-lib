package kg.onoi.smart_sdk.models

import org.parceler.Parcel

@Parcel
data class RecognitionInfo(
    val inn: String? = null,
    val passportNumber: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val dateBirth: String? = null,
    val dateExpiry: String? = null,
    val dateIssue: String? = null,
    val authority: String? = null,
    val userDidNotChangeData: Boolean = true
)