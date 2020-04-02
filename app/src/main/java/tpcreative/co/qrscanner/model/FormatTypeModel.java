package tpcreative.co.qrscanner.model;
import androidx.annotation.NonNull;

public class FormatTypeModel {
    public String id;
    public String name;
    public FormatTypeModel(String id,String name){
        this.id = id;
        this.name  = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
