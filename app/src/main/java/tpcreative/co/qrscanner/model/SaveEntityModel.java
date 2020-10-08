package tpcreative.co.qrscanner.model;

import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.entities.SaveEntity;

public class SaveEntityModel {
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

    public SaveEntityModel(SaveEntity item){
        this.id = item.id;
        this.email = item.email;
        this.subject = item.subject;
        this.message = item.message;
        this.phone = item.phone;
        this.lat = item.lat;
        this.lon = item.lon;
        this.query = item.query;
        this.title = item.title;
        this.location = item.location;
        this.description = item.description;
        this.startEvent= item.startEvent;
        this.endEvent = item.endEvent;
        this.startEventMilliseconds = item.startEventMilliseconds;
        this.endEventMilliseconds = item.endEventMilliseconds;
        this.fullName = item.fullName;
        this.address = item.address;
        this.text = item.text;
        this.ssId = item.ssId;
        this.hidden = item.hidden;
        this.password = item.password;
        this.url= item.url;
        this.createType = item.createType;
        this.networkEncryption = item.networkEncryption;
        this.createDatetime = item.createDatetime;
        this.barcodeFormat = item.barcodeFormat;
        this.favorite = item.favorite;
        this.updatedDateTime = item.updatedDateTime;
        this.contentUnique = item.contentUnique;
        this.contentUniqueForUpdatedTime = item.contentUnique +""+this.updatedDateTime;
    }

    public SaveEntityModel(SaveModel item){
        this.id = item.id;
        this.email = item.email;
        this.subject = item.subject;
        this.message = item.message;
        this.phone = item.phone;
        this.lat = item.lat;
        this.lon = item.lon;
        this.query = item.query;
        this.title = item.title;
        this.location = item.location;
        this.description = item.description;
        this.startEvent= item.startEvent;
        this.endEvent = item.endEvent;
        this.startEventMilliseconds = item.startEventMilliseconds;
        this.endEventMilliseconds = item.endEventMilliseconds;
        this.fullName = item.fullName;
        this.address = item.address;
        this.text = item.text;
        this.ssId = item.ssId;
        this.hidden = item.hidden;
        this.password = item.password;
        this.url= item.url;
        this.createType = item.createType;
        this.networkEncryption = item.networkEncryption;
        this.createDatetime = item.createDatetime;
        this.barcodeFormat = item.barcodeFormat;
        this.favorite = item.favorite;
        this.updatedDateTime = item.updatedDateTime;
        this.contentUnique = Utils.getCodeContentByGenerate(item);
        this.contentUniqueForUpdatedTime = this.contentUnique +""+this.updatedDateTime;
    }
}
