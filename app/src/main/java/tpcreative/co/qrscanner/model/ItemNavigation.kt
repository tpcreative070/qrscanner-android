package tpcreative.co.qrscanner.model
import com.google.zxing.client.result.ParsedResultType
import java.io.Serializable

class ItemNavigation(var resultType: ParsedResultType?, var enumFragmentType: EnumFragmentType?, var enumAction: EnumAction?, var res: Int, var value: String?) : Serializable