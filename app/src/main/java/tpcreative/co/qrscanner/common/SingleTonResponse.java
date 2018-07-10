package tpcreative.co.qrscanner.common;

public class SingleTonResponse {

    private static SingleTonResponse instance ;
    private SingleTonResponseListener listener;

    public static SingleTonResponse getInstance(){
        if (instance==null){
            synchronized (SingleTonResponse.class){
                if (instance==null){
                    instance = new SingleTonResponse();
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

    public interface SingleTonResponseListener{
        void showScannerPosition();
        void showCreatePosition();
    }


}
