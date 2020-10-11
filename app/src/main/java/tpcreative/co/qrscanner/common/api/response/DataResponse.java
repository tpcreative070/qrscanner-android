package tpcreative.co.qrscanner.common.api.response;
import java.io.Serializable;

import tpcreative.co.qrscanner.common.api.request.CheckoutRequest;

public class DataResponse  implements Serializable {
    public CheckoutRequest checkout;
}
