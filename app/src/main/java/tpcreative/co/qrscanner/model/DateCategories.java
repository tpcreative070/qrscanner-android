package tpcreative.co.qrscanner.model;

public class DateCategories {
    public int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String date;

    public DateCategories(){
        this.id = 0;
        this.date = "";
    }

    public DateCategories(int id, String date){
        this.id = id;
        this.date = date;
    }
}
