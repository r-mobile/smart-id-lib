package kg.onoi.smart_sdk.models

enum class PhotoType {
    PASSPORT_FRONT,
    PASSPORT_BACK,
    SELFIE,
    SELFIE_W_PASSPORT;

    fun isFacing(): Boolean = this in arrayOf(
        SELFIE,
        SELFIE_W_PASSPORT
    )
}