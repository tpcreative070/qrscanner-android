package tpcreative.co.qrscanner.helper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    private  static TimeHelper mInstance;
    private TimeHelper(){}
    public static TimeHelper getInstance(){
        if (mInstance==null){
            mInstance = new TimeHelper();
        }
        return mInstance;
    }

    public Date getDateTime(){
        return new Date();
    }
}
