package tpcreative.co.qrscanner.model;
import com.google.gson.Gson;
import java.io.Serializable;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class Author implements Serializable{

    private static Author instance;
    public Version version;

    private static final String TAG = Author.class.getSimpleName();

    public static Author getInstance(){
        if (instance==null){
            instance = new Author();
        }
        return instance;
    }

    public Author getAuthorInfo(){
        try{
            String value = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_author),null);
            if (value!=null){
                final Author author = new Gson().fromJson(value,Author.class);
                if (author!=null){
                    Utils.Log(TAG,new Gson().toJson(author));
                    return author;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new Author();
    }

}
