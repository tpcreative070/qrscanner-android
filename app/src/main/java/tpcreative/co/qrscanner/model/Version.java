package tpcreative.co.qrscanner.model;

import java.io.Serializable;
import java.util.HashMap;

public class Version implements Serializable{
    public String title;
    public boolean release;
    public boolean isShowFamilyApps;
    public boolean isAds;
    public boolean isProVersion;
    public String version_name;
    public int version_code;
    public Ads ads;
    public HashMap<Object,String>content;
}
