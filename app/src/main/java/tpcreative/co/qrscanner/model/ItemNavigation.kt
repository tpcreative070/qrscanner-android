package tpcreative.co.qrscanner.model
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import java.io.Serializable

class ItemNavigation(var resultType: ParsedResultType?,val contactKey: String,val contactValue : String,var barcodeFormat: String?, var enumFragmentType: EnumFragmentType?, var enumAction: EnumAction?, var res: Int, var value: String?, var isFavorite : Boolean?) : Serializable