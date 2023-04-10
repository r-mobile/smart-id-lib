package kg.onoi.smart_sdk.models


data class ConfirmationDocumentResponse(val title:String, val documents: List<ConfirmationDocument>)

data class ConfirmationDocument(val documentType: String, val text: String, val linkText: String)