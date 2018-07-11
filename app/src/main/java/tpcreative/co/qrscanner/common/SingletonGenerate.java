package tpcreative.co.qrscanner.common;

public class SingletonGenerate {

    private static SingletonGenerate instance ;
    private SingletonGenerateListener listener;

    public static SingletonGenerate getInstance(){
        if (instance==null){
            synchronized (SingletonGenerate.class){
                if (instance==null){
                    instance = new SingletonGenerate();
                }
            }
        }
        return instance;
    }

    public void setListener(SingletonGenerateListener listener){
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

    public interface SingletonGenerateListener{
        void setVisible();
        void setInvisible();
    }

}
