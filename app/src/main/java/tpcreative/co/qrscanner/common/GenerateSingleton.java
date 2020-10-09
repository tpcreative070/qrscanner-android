package tpcreative.co.qrscanner.common;
public class GenerateSingleton {
    private static GenerateSingleton instance ;
    private SingletonGenerateListener listener;

    public static GenerateSingleton getInstance(){
        if (instance==null){
            synchronized (GenerateSingleton.class){
                if (instance==null){
                    instance = new GenerateSingleton();
                }
            }
        }
        return instance;
    }
    public void setListener(SingletonGenerateListener listener){
        this.listener = listener;
    }

    public void onCompletedGenerate(){
        if (listener!=null){
            listener.onCompletedGenerate();
        }
    }
    public interface SingletonGenerateListener{
        void onCompletedGenerate();
    }
}
