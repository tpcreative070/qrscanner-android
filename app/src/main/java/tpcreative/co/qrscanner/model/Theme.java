package tpcreative.co.qrscanner.model;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;


public class Theme implements Serializable {
    private int id;
    private int primaryColor;
    private int primaryDarkColor;
    private int accentColor;
    public boolean isCheck;

    private static final String TAG = Theme.class.getSimpleName();

    private static Theme instance ;

    public static Theme getInstance(){
        if (instance==null){
            instance = new Theme();
        }
        return instance;
    }

    public Theme(){

    }

    public Theme(int primaryColor, int primaryDarkColor, int accentColor) {
        this.primaryColor = primaryColor;
        this.primaryDarkColor = primaryDarkColor;
        this.accentColor = accentColor;
        this.isCheck = false;
    }

    public Theme(int id , int primaryColor, int primaryDarkColor, int accentColor) {
        this.id = id;
        this.primaryColor = primaryColor;
        this.primaryDarkColor = primaryDarkColor;
        this.accentColor = accentColor;
        this.isCheck = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
    }

    public int getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public void setPrimaryDarkColor(int primaryDarkColor) {
        this.primaryDarkColor = primaryDarkColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }


    public Theme getThemeInfo(){
        try{
            String value = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_theme_object),null);
            if (value!=null){
                final Theme mTheme = new Gson().fromJson(value,Theme.class);
                if (mTheme!=null){
                    return mTheme;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new Theme(0, R.color.black, R.color.colorDark, R.color.colorButton);
    }


    public List<Theme> getDefaultThemeList(){
        try{
            String result = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_theme_list),null);
            Type listType = new TypeToken<ArrayList<Theme>>(){}.getType();
            List<Theme> mList = new Gson().fromJson(result, listType);
            return mList;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<Theme> getList(){
        try{
            final List<Theme> value = getDefaultThemeList();
            final int current_code_version = PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version),0);
            if (value!=null && current_code_version == BuildConfig.VERSION_CODE){
                Utils.Log(TAG,"Already install this version");
                return value;
            }
            else{
               final List<Theme> mList = new ArrayList<>();
               mList.addAll(ThemeUtil.getThemeList());
               PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_theme_list),new Gson().toJson(mList));
               PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version),BuildConfig.VERSION_CODE);
               PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.we_are_a_team),false);
               Utils.Log(TAG,"New install this version");
               return mList;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
