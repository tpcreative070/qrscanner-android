package tpcreative.co.qrscanner.model;
import com.google.zxing.client.result.ParsedResultType;

import java.io.Serializable;

public class Create implements Serializable {

    public int id;
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
    public long   startEventMilliseconds;
    public long   endEventMilliseconds;
    public String endEvent;
    public String fullName;
    public String address;
    public String text;
    public String productId;
    public String ISBN;
    public String ssId;
    public boolean hidden;
    public String password;
    public String url;
    public ParsedResultType createType;
    public String networkEncryption;
    public EnumFragmentType fragmentType;
    public EnumImplement enumImplement;



    public Create(){
        email = "";
        subject = "";
        message = "";
        phone = "";
        lat = 0.0;
        lon = 0.0;
        startEventMilliseconds = 0;
        endEventMilliseconds = 0;
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
        hidden = false;
        createType = ParsedResultType.TEXT;
        fragmentType = EnumFragmentType.SCANNER;
        enumImplement = EnumImplement.CREATE;
        id = 0;
    }

    public Create(String title){
        email = "";
        subject = "";
        message = "";
        phone = "";
        lat = 0.0;
        lon = 0.0;
        startEventMilliseconds = 0;
        endEventMilliseconds = 0;
        query = "";
        this.title = title;
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
        hidden = false;
        createType = ParsedResultType.TEXT;
        fragmentType = EnumFragmentType.SCANNER;
        enumImplement = EnumImplement.CREATE;
        id = 0;
    }
}
