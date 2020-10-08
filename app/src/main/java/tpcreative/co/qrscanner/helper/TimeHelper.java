package tpcreative.co.qrscanner.helper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    private  static TimeHelper mInstance;
    private TimeHelper(){}
    final static String mStandardSortedDateTime = "ddMMYYYYHHmmss";
    final static String FORMAT_DISPLAY = "EE dd MMM, yyyy HH:mm:ss a";
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

    public String getCurrentDateDisplay(String value) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dateFormat.parse(value);
            dateFormat = new SimpleDateFormat(FORMAT_DISPLAY,Locale.getDefault());
            String result = dateFormat.format(date);
            return result;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return value;
    }
}
