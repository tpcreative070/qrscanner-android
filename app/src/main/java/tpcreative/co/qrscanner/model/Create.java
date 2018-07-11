package tpcreative.co.qrscanner.model;
import java.io.Serializable;

public class Create implements Serializable {

    public String email;
    public String subject ;
    public String message;
    public String phone;
    public double lat;
    public double lon;
    public String query;
    public String title;
    public String location;
    public String description;
    public String startEvent;
    public String endEvent;
    public String fullName;
    public String address;
    public String text;
    public String ssId;
    public String password;
    public String url;
    public EnumCreateType createType;
    public EnumNetworkType networkType;

    public Create(){
        email = "";
        subject = "";
        message = "";
        phone = "";
        lat = 0.0;
        lon = 0.0;
        query = "";
        title = "";
        location = "";
        description = "";
        startEvent = "";
        endEvent = "";
        fullName = "";
        address = "";
        text = "";
        ssId = "";
        password = "";
        url = "";
        createType = EnumCreateType.EMAIL;
        networkType = EnumNetworkType.NONE;
    }

}
