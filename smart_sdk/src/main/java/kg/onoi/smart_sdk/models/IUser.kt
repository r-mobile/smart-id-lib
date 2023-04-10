package kg.onoi.smart_sdk.models

interface IUser {
    fun getUserInn(): String
    fun getCompanyInn(): String?
    fun getUserName(): String
    fun getCompanyName(): String?

    fun isSimpleUser() = getCompanyInn().isNullOrEmpty() && getCompanyName().isNullOrEmpty()
}