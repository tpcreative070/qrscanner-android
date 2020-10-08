package tpcreative.co.qrscanner.common;

public class SaveSingleton {
    private static SaveSingleton instance ;
    private SingletonSaveListener listener;

    public static SaveSingleton getInstance(){
        if (instance==null){
            synchronized (SaveSingleton.class){
                if (instance==null){
                    instance = new SaveSingleton();
                }
            }
        }
        return instance;
    }

    public void setListener(SingletonSaveListener listener){
        this.listener = listener;
    }

    public void reLoadData(){
        if (listener!=null){
            listener.reLoadData();
        }
    }

    public interface SingletonSaveListener{
        void reLoadData();
    }


}
