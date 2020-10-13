package tpcreative.co.qrscanner.common;

public class BackupSingleton {
    private static BackupSingleton instance ;
    private BackupSingleton.BackupSingletonListener listener;
    public static BackupSingleton getInstance(){
        if (instance==null){
            synchronized (BackupSingleton.class){
                if (instance==null){
                    instance = new BackupSingleton();
                }
            }
        }
        return instance;
    }
    public void setListener(BackupSingleton.BackupSingletonListener listener){
        this.listener = listener;
    }
    public void reloadData(){
        if (listener!=null){
            listener.reloadData();
        }
    }
    public interface BackupSingletonListener{
        void reloadData();
    }
}
