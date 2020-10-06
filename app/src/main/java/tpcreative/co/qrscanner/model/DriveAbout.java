package tpcreative.co.qrscanner.model;
import java.io.Serializable;
import java.util.List;
import tpcreative.co.qrscanner.common.api.response.BaseResponseDrive;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;

public class DriveAbout extends BaseResponseDrive implements Serializable{
    public long inAppUsed;
    public DriveUser user;
    public StorageQuota storageQuota;
    /*Create folder*/
    /*Drive api queries*/
    public List<DriveResponse> files;
}
