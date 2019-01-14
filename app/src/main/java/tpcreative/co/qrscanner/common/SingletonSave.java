package tpcreative.co.qrscanner.common;

public class SingletonSave {
    private static SingletonSave instance ;
    private SingletonSaveListener listener;

    public static SingletonSave getInstance(){
        if (instance==null){
            synchronized (SingletonSave.class){
                if (instance==null){
                    instance = new SingletonSave();
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
