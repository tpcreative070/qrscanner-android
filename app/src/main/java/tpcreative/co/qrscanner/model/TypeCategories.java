package tpcreative.co.qrscanner.model;

import java.io.Serializable;

public class TypeCategories implements Serializable{
    public int id;
    public String type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public TypeCategories(){
        this.id = 0;
        this.type = "";
    }

    public TypeCategories(int id, String type){
        this.id = id;
        this.type = type;
    }

}
