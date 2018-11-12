package tpcreative.co.qrscanner.common.api.response;
import com.google.gson.Gson;
import java.io.Serializable;

import tpcreative.co.qrscanner.model.Version;

public class BaseResponse implements Serializable {
    public String message ;
    public boolean error ;
    public String nextPage;
    public Version version;
    public String toFormResponse() {
        return new Gson().toJson(this);
    }
}

