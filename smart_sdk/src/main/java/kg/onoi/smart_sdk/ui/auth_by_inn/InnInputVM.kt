package kg.onoi.smart_sdk.ui.auth_by_inn

import com.google.gson.JsonObject
import kg.onoi.smart_sdk.models.*
import kg.onoi.smart_sdk.repositories.AuthRepo
import kg.onoi.smart_sdk.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class InnInputVM : BaseViewModel() {

    private val authRepo: AuthRepo by lazy { AuthRepo() }

    fun requestNameByInn(inn: String) {
        vmCoroutineScope.launch {
            try {
                handleFetchedName(authRepo.fetchNameByInn(inn))
            } catch (e: Exception) {
                handleFetchedName(null)
            }
        }
    }

    private fun handleFetchedName(name: Name?) {
        event.value = if (name != null) InnAuthEvent.SuccessFetchedName(name.name)
        else InnAuthEvent.MissingFetchedName()
    }

    fun checkInn(inn: String) {
        runWithProgress {
            try {
                handleCheckInn(authRepo.checkInn(inn))
            } catch (e: Throwable) {
                if (e is HttpException && e.code() == 400) {
                    try {
                        val code = JSONObject(e.response()?.errorBody()?.string()).getInt("code")
                        event.value = if(code == 1) InnAuthEvent.UserNotFound() else Event.UnknownErrorEvent()
                    } catch (e: Exception) { throw e }
                }
            }
        }
    }

    private fun handleCheckInn(it: InnCheck?) {
        event.value = when (it?.next) {
            NextStepType.GiveMeInn -> InnAuthEvent.InputEmployeeInnEvent(it.inn)
            NextStepType.GiveMePin -> InnAuthEvent.InputPinEvent(it.inn, it.isPinPermanent)
            else -> Event.UnknownErrorEvent()
        }
    }

    fun checkEmployeeInn(orgInn: String, employeeInn: String) {
        runWithProgress {
            handleCheckEmployeeInn(authRepo.checkEmployeeInn(orgInn, employeeInn))
        }
    }

    private fun handleCheckEmployeeInn(employee: EmployeeInnCheck?) {
        event.value = when (employee?.next) {
            NextStepType.GiveMePin -> InnAuthEvent.InputPinEvent(
                employee.innEmployee,
                employee.isPinPermanent
            )
            else -> Event.UnknownErrorEvent()
        }
    }
}