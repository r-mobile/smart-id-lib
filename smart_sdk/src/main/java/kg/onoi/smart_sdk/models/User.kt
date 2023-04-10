package kg.onoi.smart_sdk.models

import kg.onoi.smart_sdk.recognition.ResultRecognitionWrapper
import org.parceler.Parcel

@Parcel
data class User(
    var sessionId: String? = null,
    var userType: UserType? = UserType.INDIVIDUAL,
    var phoneNumber: String? = null,
    var documentType: DocumentType? = null,

    var pin: String? = null,
    var passportNumber: String? = null,
    var name: String? = null,
    var surname: String? = null,
    var patronymic: String? = null,

    var dateBirth: String? = null,
    var dateExpiry: String? = null,
    var dateIssue: String? = null,
    var authority: String? = null,

    var frontRecognitionInfo: ResultRecognitionWrapper? = null,
    var backRecognitionInfo: ResultRecognitionWrapper? = null,

    var isCompleted: Boolean = false,
    var isApproved: Boolean = false

) {
    constructor(sessionId: String, userData: RecognitionInfo) : this(
        sessionId,
        pin = userData.inn,
        name = userData.name,
        surname = userData.surname,
        patronymic = userData.patronymic,
        passportNumber = userData.passportNumber,
        dateBirth = userData.dateBirth,
        dateExpiry = userData.dateExpiry,
        dateIssue = userData.dateIssue,
        authority = userData.authority
    )
}

