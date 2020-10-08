package tpcreative.co.qrscanner.common;

public class SettingsSingleton {

    private static SettingsSingleton instance ;
    private SettingsSingleton.SingletonSettingsListener listener;

    public static SettingsSingleton getInstance(){
        if (instance==null){
            synchronized (SettingsSingleton.class){
                if (instance==null){
                    instance = new SettingsSingleton();
                }
            }
        }
        return instance;
    }

    public void setListener(SettingsSingleton.SingletonSettingsListener listener){
        this.listener = listener;
    }

    public void onUpdated(){
        if (listener!=null){
            listener.onUpdated();
        }
    }

    public void onUpdateSharePreference(boolean value){
        if (listener!=null){
            listener.onUpdatedSharePreferences(value);
        }
    }

    public interface SingletonSettingsListener{
        void onUpdated();
        void onUpdatedSharePreferences(boolean value);
    }
}
