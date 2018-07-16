package tpcreative.co.qrscanner.common;

public class SingletonCloseFragment {

    private static SingletonCloseFragment instance ;
    private SingletonCloseFragmentListener listener;
    private boolean isUpdateDate;

    public static SingletonCloseFragment getInstance(){
        if (instance==null){
            synchronized (SingletonCloseFragment.class){
                if (instance==null){
                    instance = new SingletonCloseFragment();
                }
            }
        }
        return instance;
    }

    public void setListener(SingletonCloseFragmentListener listener){
        this.listener = listener;
    }


    public void setCloseWindow(){
        if (listener!=null){
            listener.onCloseWindow();
        }
    }

    public void setUpdateData(boolean isUpdateDate){
        this.isUpdateDate = isUpdateDate;
    }

    public boolean isCloseWindow() {
        return isUpdateDate;
    }

    public interface SingletonCloseFragmentListener{
        void onCloseWindow();
    }
}
