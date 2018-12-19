package tpcreative.co.qrscanner.model;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.zxing.BarcodeFormat;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "save")
public class Save implements Serializable{
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
    //public String barcodeFormat;

    @Ignore
    public TypeCategories typeCategories;
    @Ignore
    private boolean isChecked;
    @Ignore
    private boolean isDeleted;

    public Save(
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
                String createDatetime){

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
        //this.barcodeFormat = BarcodeFormat.QR_CODE.name();
    }

    public Save(){
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
        this.typeCategories = new TypeCategories();
        //this.barcodeFormat = BarcodeFormat.QR_CODE.name();
    }

    @Ignore
    public boolean isChecked() {
        return isChecked;
    }

    @Ignore
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Ignore
    public boolean isDeleted() {
        return isDeleted;
    }

    @Ignore
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    @Ignore
    public int getId() {
        return id;
    }

    @Ignore
    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    @Ignore
    public int getCategoryId() {
        return typeCategories.getId();
    }

    @Ignore
    public String getCategoryName() {
        return typeCategories.getType();
    }

}

