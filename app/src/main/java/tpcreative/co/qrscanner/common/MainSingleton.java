package tpcreative.co.qrscanner.common;

public class MainSingleton {
    private static MainSingleton instance ;
    private SingleTonMainListener listener;

    public static MainSingleton getInstance(){
        if (instance==null){
            synchronized (MainSingleton.class){
                if (instance==null){
                    instance = new MainSingleton();
                }
            }
        }
        return instance;
    }

    public void setListener(SingleTonMainListener listener){
        this.listener = listener;
    }

    public void isShowDeleteAction(boolean isDelete){
        if (listener!=null){
            listener.isShowDeleteAction(isDelete);
        }
    }

    public interface SingleTonMainListener{
        void isShowDeleteAction(boolean isDelete);
    }
}
