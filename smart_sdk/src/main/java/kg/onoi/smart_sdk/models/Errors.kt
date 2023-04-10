package kg.onoi.smart_sdk.models

class ServerError(override val message: String?) : Exception(message)
class NoDigitalSignatureException(override val message: String) : Exception(message)