package tpcreative.co.qrscanner.common;

public class SingletonHistory {
    private static SingletonHistory instance ;
    private SingletonHistoryListener listener;

    public static SingletonHistory getInstance(){
        if (instance==null){
            synchronized (SingletonHistory.class){
                if (instance==null){
                    instance = new SingletonHistory();
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
