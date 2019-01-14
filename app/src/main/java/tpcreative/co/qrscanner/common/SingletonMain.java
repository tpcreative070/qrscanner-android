package tpcreative.co.qrscanner.common;

public class SingletonMain {
    private static SingletonMain instance ;
    private SingleTonMainListener listener;

    public static SingletonMain getInstance(){
        if (instance==null){
            synchronized (SingletonMain.class){
                if (instance==null){
                    instance = new SingletonMain();
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
