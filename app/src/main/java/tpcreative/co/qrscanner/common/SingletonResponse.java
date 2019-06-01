package tpcreative.co.qrscanner.common;
public class SingletonResponse {

    private static SingletonResponse instance ;
    private SingleTonResponseListener listener;

    public static SingletonResponse getInstance(){
        if (instance==null){
            synchronized (SingletonResponse.class){
                if (instance==null){
                    instance = new SingletonResponse();
                }
            }
        }
        return instance;
    }

    public void setListener(SingleTonResponseListener listener){
        this.listener = listener;
    }

    public void setScannerPosition(){
        if (listener!=null){
            listener.showScannerPosition();
        }
    }

    public void setCreatePosition(){
        if (listener!=null){
            listener.showCreatePosition();
        }
    }

    public void onAlertLatestVersion(){
        if (listener!=null){
            listener.showAlertLatestVersion();
        }
    }

    public void onNetworkConnectionChanged(boolean isConntected){
        if (listener!=null){
            listener.onNetworkConnectionChanged(isConntected);
        }
    }

    public void onResumeAds(){
        if (listener!=null){
            listener.onResumeAds();
        }
    }

    public interface SingleTonResponseListener{
        void showScannerPosition();
        void showCreatePosition();
        void showAlertLatestVersion();
        void onNetworkConnectionChanged(boolean isConnected);
        void onResumeAds();
    }
}
