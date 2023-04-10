package kg.onoi.smart_sdk.recognition

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import biz.smartengines.smartid.swig.RecognitionResult
import kg.onoi.smart_sdk.models.DocumentType
import org.parceler.Parcel

@Parcel
class ResultRecognitionWrapper {
    var picturePath: String = ""
    var docType: String? = null
    val recognitionFields = mutableMapOf<String, String>()
    private val validDocTypes = arrayOf("kgz.id.type1", "kgz.id.type2")

    fun init(result: RecognitionResult) {
        docType = result.GetDocumentType()
        convertResultToMap(result)
    }

    fun isValidDocType(): Boolean = docType in validDocTypes

    fun getType(): String = docType ?: ""

    fun getTypeAsDocumentType(): DocumentType = when (getType()) {
        "kgz.id.type1" -> DocumentType.PASSPORT_OLD
        else -> DocumentType.PASSPORT_NEW
    }

    fun getDocImage(): Bitmap? {
        val base64 = when (getDocumentSide()) {
            DocumentSide.FRONT -> recognitionFields["${getType()}:front"]
            else -> recognitionFields["${getType()}:back"]
        }
        return if (base64.isNullOrEmpty()) null
        else {
            val bytes = Base64.decode(base64, 0)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    fun getDocumentSide() =
        when (getType()) {
            "kgz.id.type1" -> if (isType1BackSide()) DocumentSide.BACK else DocumentSide.FRONT
            "kgz.id.type2" -> if (isType2BackSide()) DocumentSide.BACK else DocumentSide.FRONT
            else -> null
        }

    private fun isType1BackSide(): Boolean = recognitionFields.keys.contains("full_mrz")

    private fun isType2BackSide(): Boolean = recognitionFields.keys.contains("full_mrz")

    private fun convertResultToMap(result: RecognitionResult) {
        result?.let {
            for (index in 0 until it.GetStringFieldNames().size()) {
                val fieldName = it.GetStringFieldNames()[index.toInt()]
                val fieldValue = it.GetStringField(fieldName).GetUtf8Value()
                recognitionFields[fieldName] = fieldValue
            }

            for (index in 0 until it.GetImageFieldNames().size()) {
                val fieldName = it.GetImageFieldNames()[index.toInt()]
                val fieldValue = it.GetImageField(fieldName).GetValue()
                val byteArray = ByteArray(fieldValue.GetRequiredBase64BufferLength())
                fieldValue.CopyBase64ToBuffer(byteArray)
                recognitionFields[fieldName] = String(byteArray)
            }
        }
    }

    fun isUnrecognized(): Boolean = docType.isNullOrEmpty()

    fun isDataOfOneDocument(result: ResultRecognitionWrapper?): Boolean {
        if (result == null) return false

        return when (result.getType()) {
            "kgz.id.type1" -> isDataOfOneDocumentOld(result)
            "kgz.id.type2" -> isDataOfOneDocumentNew(result)
            else -> false
        }
    }

    private fun isDataOfOneDocumentNew(result: ResultRecognitionWrapper): Boolean {
        return getType() == result.getType()
                && recognitionFields["number"] == result.recognitionFields["number_mrz"]
                && recognitionFields["name_eng"] == result.recognitionFields["first_name_mrz"]
                && recognitionFields["surname_eng"] == result.recognitionFields["last_name_mrz"]
                && recognitionFields["birth_date"] == result.recognitionFields["birth_date_mrz"]
    }

    private fun isDataOfOneDocumentOld(result: ResultRecognitionWrapper): Boolean {
        return getType() == result.getType()
                && recognitionFields["number"] == result.recognitionFields["number_mrz"]
                && recognitionFields["id_number"] == result.recognitionFields["opt_data_1_mrz"]
    }

    fun isSuccessRecognized(): Boolean {
        return when (getTypeAsDocumentType()) {
            DocumentType.PASSPORT_NEW -> {
                when (getDocumentSide()) {
                    DocumentSide.FRONT -> isSuccessRecognizedNewFront()
                    DocumentSide.BACK -> isSuccessRecognizedNewBack()
                    else -> false
                }
            }
            DocumentType.PASSPORT_OLD -> {
                when (getDocumentSide()) {
                    DocumentSide.FRONT -> isSuccessRecognizedOldFront()
                    DocumentSide.BACK -> isSuccessRecognizedOldBack()
                    else -> false
                }
            }
        }
    }

    private fun isSuccessRecognizedNewFront(): Boolean {
        return !recognitionFields["name"]?.trim().isNullOrEmpty()
                && !recognitionFields["patronymic"]?.trim().isNullOrEmpty()
                && !recognitionFields["surname"]?.trim().isNullOrEmpty()
                && !recognitionFields["number"]?.trim().isNullOrEmpty()
    }

    private fun isSuccessRecognizedNewBack(): Boolean {
        return !recognitionFields["id_number"]?.trim().isNullOrEmpty()
                && !recognitionFields["number"]?.trim().isNullOrEmpty()
    }

    private fun isSuccessRecognizedOldFront(): Boolean {
        return !recognitionFields["name"]?.trim().isNullOrEmpty()
                && !recognitionFields["patronymic"]?.trim().isNullOrEmpty()
                && !recognitionFields["surname"]?.trim().isNullOrEmpty()
                && !recognitionFields["id_number"]?.trim().isNullOrEmpty()
                && !recognitionFields["number"]?.trim().isNullOrEmpty()
    }

    private fun isSuccessRecognizedOldBack(): Boolean {
        return !recognitionFields["number_mrz"]?.trim().isNullOrEmpty()
                && !recognitionFields["opt_data_1_mrz"]?.trim().isNullOrEmpty()
    }

    enum class DocumentSide { FRONT, BACK }
}