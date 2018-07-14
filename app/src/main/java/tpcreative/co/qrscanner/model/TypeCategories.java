package tpcreative.co.qrscanner.model;

public class TypeCategories {
    public int id;

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

    public String type;

    public TypeCategories(){
        this.id = 0;
        this.type = "";
    }

    public TypeCategories(int id, String date){
        this.id = id;
        this.type = date;
    }

}
