package tpcreative.co.qrscanner.model;

import androidx.room.Ignore;

import com.google.zxing.BarcodeFormat;

import java.io.Serializable;
import java.util.UUID;

import tpcreative.co.qrscanner.common.Utils;

public class HistoryModel implements Serializable {
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
    public String contentUniqueForUpdatedTime;

    /*Custom fields*/
    public TypeCategories typeCategories;
    private boolean isChecked;
    private boolean isDeleted;

    public HistoryModel(){
        this.email = "";
        this.subject = "";
        this.message = "" ;
        this.phone = "";
        this.lat = 0;
        this.lon = 0;
        this.startEventMilliseconds = 0;
        this.endEventMilliseconds = 0;
        this.query = "";
        this.title = "";
        this.location = "";
        this.description = "";
        this.startEvent = "";
        this.endEvent = "";
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
        this.barcodeFormat = BarcodeFormat.QR_CODE.name();
        this.favorite = false;
        this.updatedDateTime = Utils.getCurrentDateTimeSort();
        this.contentUnique = "";
        this.contentUnique = "";
    }

    public HistoryModel(HistoryEntityModel item){
        this.id = item.id;
        this.email = item.email;
        this.subject = item.subject ;
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
        this.contentUniqueForUpdatedTime = item.contentUniqueForUpdatedTime;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    public int getCategoryId() {
        return typeCategories.getId();
    }

    public String getCategoryName() {
        return typeCategories.getType();
    }
}
