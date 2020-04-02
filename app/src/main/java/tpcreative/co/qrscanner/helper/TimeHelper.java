package tpcreative.co.qrscanner.helper;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

    private  static TimeHelper mInstance;
    private TimeHelper(){}
    static String mStandardSortedDateTime = "ddMMYYYYHHmmss";
    public static TimeHelper getInstance(){
        if (mInstance==null){
            mInstance = new TimeHelper();
        }
        return mInstance;
    }

    public Date getDateTime(){
        return new Date();
    }

    public String getString(){
        long millisecond = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat(mStandardSortedDateTime);
        String dateString = formatter.format(new Date(millisecond));
        return dateString;
    }
}
