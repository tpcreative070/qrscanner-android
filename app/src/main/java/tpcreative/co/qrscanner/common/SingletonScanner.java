package tpcreative.co.qrscanner.common;

public class SingletonScanner {
    private static SingletonScanner instance ;
    private SingletonScannerListener listener;

    public static SingletonScanner getInstance(){
        if (instance==null){
            synchronized (SingletonScanner.class){
                if (instance==null){
                    instance = new SingletonScanner();
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
