package tpcreative.co.qrscanner.model;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import java.util.UUID;

@Entity(tableName = "history")
public class History{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String time;
    public String distance ;
    public String maximum;
    public String minimum;
    public String startDatetime;
    public String stopDatetime;
    public String average;
    public String date;
    public String dateTime;
    public String key;

    @Ignore
    public DateCategories dateCategories;
    @Ignore
    private boolean isChecked;
    @Ignore
    private boolean isDeleted;

    public History(String key,String time,String date,String startDatetime,String stopDatetime,String distance,String maximum,String minimum,String average,String dateTime){
        this.time = time;
        this.startDatetime = startDatetime;
        this.stopDatetime = stopDatetime;
        this.distance = distance ;
        this.maximum = maximum;
        this.minimum = minimum;
        this.average = average;
        this.date = date;
        this.dateTime = dateTime;
        this.key = key;
    }

    public History(){
        this.time = null;
        this.startDatetime = null;
        this.stopDatetime = null;
        this.distance = null;
        this.maximum = null;
        this.minimum = null;
        this.average = null;
        this.dateCategories = new DateCategories();
    }

    @Ignore
    public boolean isChecked() {
        return isChecked;
    }

    @Ignore
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Ignore
    public boolean isDeleted() {
        return isDeleted;
    }

    @Ignore
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    @Ignore
    public String getKey() {
        return key;
    }

    @Ignore
    public void setKey(String key) {
        this.key = key;
    }

    @Ignore
    public String getDateTime() {
        return dateTime;
    }

    @Ignore
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Ignore
    public int getId() {
        return id;
    }

    @Ignore
    public void setId(int id) {
        this.id = id;
    }

    @Ignore
    public String getTime() {
        return time;
    }

    @Ignore
    public void setTime(String time) {
        this.time = time;
    }

    @Ignore
    public String getDistance() {
        return distance;
    }

    @Ignore
    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Ignore
    public String getMaximum() {
        return maximum;
    }

    @Ignore
    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    @Ignore
    public String getMinimum() {
        return minimum;
    }

    @Ignore
    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    @Ignore
    public String getStartDatetime() {
        return startDatetime;
    }

    @Ignore
    public void setStartDatetime(String startDatetime) {
        this.startDatetime = startDatetime;
    }

    @Ignore
    public String getStopDatetime() {
        return stopDatetime;
    }

    @Ignore
    public void setStopDatetime(String stopDatetime) {
        this.stopDatetime = stopDatetime;
    }

    @Ignore
    public String getAverage() {
        return average;
    }

    @Ignore
    public void setAverage(String average) {
        this.average = average;
    }

    @Ignore
    public String getDate() {
        return date;
    }

    @Ignore
    public void setDate(String date) {
        this.date = date;
    }

    @Ignore
    public String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    @Ignore
    public int getCategoryId() {
        return dateCategories.getId();
    }

    @Ignore
    public String getCategoryName() {
        return dateCategories.getDate();
    }

}

