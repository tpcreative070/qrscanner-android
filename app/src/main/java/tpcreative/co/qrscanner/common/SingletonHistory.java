package tpcreative.co.qrscanner.common;

public class SingletonHistory {
    private static SingletonHistory instance ;
    private SingletonHistoryListener listener;
    private boolean isUpdateDate;

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

    public interface SingletonHistoryListener{
        void setVisible();
        void setInvisible();
    }

}
