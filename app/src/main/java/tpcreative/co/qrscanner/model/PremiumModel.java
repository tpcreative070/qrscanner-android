package tpcreative.co.qrscanner.model;

import java.io.Serializable;

public class PremiumModel implements Serializable {
    public boolean isPremium;
    public PremiumModel(boolean isPremium){
        this.isPremium = isPremium;
    }
}
