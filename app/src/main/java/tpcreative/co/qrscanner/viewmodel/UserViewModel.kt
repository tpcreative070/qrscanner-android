package tpcreative.co.qrscanner.viewmodel
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import tpcreative.co.qrscanner.common.api.requester.UserService
import tpcreative.co.qrscanner.common.api.response.RootResponse
import tpcreative.co.qrscanner.model.EmptyModel

class UserViewModel(private val userService : UserService)  :  BaseViewModel<EmptyModel>() {
    suspend fun checkout(request : CheckoutRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO){
            try {
                val mResult = userService.checkout(request)
                when(mResult.status){
                    Status.SUCCESS -> {
                       Resource.success(mResult.data)
                    }else ->{
                    Resource.error(mResult.code ?: Utils.CODE_EXCEPTION,mResult.message ?:"",null)
                }
                }
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }
}