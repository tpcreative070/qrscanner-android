package tpcreative.co.qrscanner.common;

public class SingletonSettings {

    private static SingletonSettings instance ;
    private SingletonSettings.SingletonSettingsListener listener;

    public static SingletonSettings getInstance(){
        if (instance==null){
            synchronized (SingletonSettings.class){
                if (instance==null){
                    instance = new SingletonSettings();
                }
            }
        }
        return instance;
    }

    public void setListener(SingletonSettings.SingletonSettingsListener listener){
        this.listener = listener;
    }

    public void onUpdated(){
        if (listener!=null){
            listener.onUpdated();
        }
    }

    public interface SingletonSettingsListener{
        void onUpdated();
    }
}
