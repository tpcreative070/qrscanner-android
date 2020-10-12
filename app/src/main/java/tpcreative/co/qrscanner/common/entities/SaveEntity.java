package tpcreative.co.qrscanner.common.entities;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.zxing.BarcodeFormat;
import java.io.Serializable;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.SaveEntityModel;

@Entity(tableName = "save")
public class SaveEntity implements Serializable{
    @PrimaryKey(autoGenerate = true)
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
    public String endEvent;
    public long   startEventMilliseconds;
    public long   endEventMilliseconds;
    public String fullName;
    public String address;
    public String text;
    public String ssId;
    public boolean hidden;
    public String password;
    public String url;
    public String createType;
    public String networkEncryption;
    public String createDatetime;
    public String barcodeFormat;
    public boolean favorite;
    public String updatedDateTime;

    /*content_type_barcode*/
    public String contentUnique;
    /*sync data*/
    public boolean isSynced;
    public String uuId;

    public SaveEntity(
                String email,
                String subject ,
                String message,
                String phone,
                double lat,
                double lon,
                String query,
                String title,
                String location,
                String description,
                String startEvent,
                String endEvent,
                long startEventMilliseconds,
                long endEventMilliseconds,
                String fullName,
                String address,
                String text,
                String ssId,
                boolean hidden,
                String password,
                String url,
                String createType,
                String networkEncryption,
                String createDatetime,BarcodeFormat barcodeFormat,String updatedDateTime){

        this.email = email;
        this.subject = subject;
        this.message = message ;
        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
        this.query = query;
        this.title = title;
        this.location = location;
        this.description = description;
        this.startEvent = startEvent;
        this.startEventMilliseconds  = startEventMilliseconds;
        this.endEventMilliseconds = endEventMilliseconds;
        this.endEvent = endEvent;
        this.fullName = fullName;
        this.address = address;
        this.text = text;
        this.ssId = ssId;
        this.hidden = hidden;
        this.password = password;
        this.url = url;
        this.createType = createType;
        this.networkEncryption = networkEncryption;
        this.createDatetime = createDatetime;
        this.barcodeFormat = barcodeFormat.name();
        this.favorite = false;
        this.updatedDateTime = updatedDateTime;
    }

    public SaveEntity(){
        this.email = "";
        this.subject = "";
        this.message = "" ;
        this.phone = "";
        this.lat = 0;
        this.lon = 0;
        this.query = "";
        this.title = "";
        this.location = "";
        this.description = "";
        this.startEvent = "";
        this.endEvent = "";
        this.startEventMilliseconds = 0;
        this.endEventMilliseconds = 0;
        this.fullName = "";
        this.address = "";
        this.text = "";
        this.ssId = "";
        this.hidden = false;
        this.password = "";
        this.url = "";
        this.createType = "";
        this.networkEncryption = "";
        this.barcodeFormat = BarcodeFormat.QR_CODE.name();
        this.favorite = false;
        this.updatedDateTime = Utils.getCurrentDateTimeSort();
    }

    public SaveEntity(SaveEntityModel item){
        this.id = item.id;
        this.email = item.email;
        this.subject  = item.subject;
        this.message = item.message;
        this.phone = item.phone;
        this.lat = item.lat;
        this.lon = item.lon;
        this.query = item.query;
        this.title = item.title;
        this.location = item.location;
        this.description = item.description;
        this.startEvent = item.startEvent;
        this.endEvent = item.endEvent;
        this.startEventMilliseconds = item.startEventMilliseconds;
        this.endEventMilliseconds = item.endEventMilliseconds;
        this.fullName = item.fullName;
        this.address = item.address;
        this.text = item.text;
        this.ssId = item.ssId;
        this.hidden = item.hidden;
        this.password = item.password;
        this.url = item.url;
        this.createType = item.createType;
        this.networkEncryption = item.networkEncryption;
        this.createDatetime = item.createDatetime;
        this.barcodeFormat = item.barcodeFormat;
        this.favorite = item.favorite;
        this.updatedDateTime = item.updatedDateTime;
        this.contentUnique = item.contentUnique;
        this.isSynced = item.isSynced;
        this.uuId = item.uuId;
    }
}

