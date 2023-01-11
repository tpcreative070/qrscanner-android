package tpcreative.co.qrscanner.common.api.requester
import co.tpcreative.supersafe.common.network.Resource
import tpcreative.co.qrscanner.common.network.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import tpcreative.co.qrscanner.common.api.response.RootResponse
import tpcreative.co.qrscanner.helper.ApiHelper

class UserService {
    suspend fun checkout(request: CheckoutRequest) : Resource<RootResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onCheckout(request)
                ResponseHandler.handleSuccess(mResult as RootResponse)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }
}