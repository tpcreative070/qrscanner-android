package tpcreative.co.qrscanner.common;

public class HistorySingleton {
    private static HistorySingleton instance ;
    private SingletonHistoryListener listener;

    public static HistorySingleton getInstance(){
        if (instance==null){
            synchronized (HistorySingleton.class){
                if (instance==null){
                    instance = new HistorySingleton();
                }
            }
        }
        return instance;
    }

    public void setListener(SingletonHistoryListener listener){
        this.listener = listener;
    }

    public void reLoadData(){
        if (listener!=null){
            listener.reLoadData();
        }
    }

    public interface SingletonHistoryListener{
        void reLoadData();
    }

}
