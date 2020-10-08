package tpcreative.co.qrscanner.common;

public class ScannerSingleton {
    private static ScannerSingleton instance ;
    private SingletonScannerListener listener;

    public static ScannerSingleton getInstance(){
        if (instance==null){
            synchronized (ScannerSingleton.class){
                if (instance==null){
                    instance = new ScannerSingleton();
                }
            }
        }
        return instance;
    }

    public void setListener(SingletonScannerListener listener){
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

    public interface SingletonScannerListener{
        void setVisible();
        void setInvisible();
    }

}
