package tpcreative.co.qrscanner.common;

public class SingletonSave {
    private static SingletonSave instance ;
    private SingletonSaveListener listener;
    private boolean isUpdateDate;

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

    public void setVisible(){
        if (listener!=null){
            listener.setVisible();
        }
    }

    public void setInvisible(){
        if (listener!=null){
            listener.setInvisible();
        }
    }

    public void setUpdateData(boolean isUpdateDate){
        this.isUpdateDate = isUpdateDate;
    }

    public boolean isUpdateData() {
        return isUpdateDate;
    }

    public interface SingletonSaveListener{
        void setVisible();
        void setInvisible();
    }

}
