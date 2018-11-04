package tpcreative.co.qrscanner.model;
import java.util.ArrayList;
import tpcreative.co.qrscanner.R;

/**
 * Created by Pankaj on 12-11-2017.
 */

public class ThemeUtil {

    public static ArrayList<Theme> getThemeList(){
        ArrayList<Theme> themeArrayList = new ArrayList<>();
        themeArrayList.add(new Theme(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton));
        themeArrayList.add(new Theme(1, R.color.inbox_primary, R.color.inbox_primary_dark, R.color.colorAccent));
        themeArrayList.add(new Theme(2, R.color.black, R.color.colorDark, R.color.colorButton));
        themeArrayList.add(new Theme(3,R.color.primaryColorPink, R.color.primaryDarkColorPink, R.color.secondaryColorPink));
        themeArrayList.add(new Theme(4,R.color.primaryColorPurple, R.color.primaryDarkColorPurple, R.color.secondaryColorPurple));
        themeArrayList.add(new Theme(5,R.color.primaryColorDeepPurple, R.color.primaryDarkColorDeepPurple, R.color.secondaryColorDeepPurple));
        themeArrayList.add(new Theme(6,R.color.primaryColorCyan, R.color.primaryDarkColorCyan, R.color.secondaryColorCyan));
        themeArrayList.add(new Theme(7,R.color.primaryColorTeal, R.color.primaryDarkColorTeal, R.color.secondaryColorTeal));
        themeArrayList.add(new Theme(8,R.color.primaryColorLightGreen, R.color.primaryDarkColorLightGreen, R.color.secondaryColorLightGreen));
        themeArrayList.add(new Theme(9,R.color.primaryColorLime, R.color.primaryDarkColorLimeStrong, R.color.secondaryColorLime));
        themeArrayList.add(new Theme(10,R.color.primaryColorAmber, R.color.primaryDarkColorAmberStrong, R.color.secondaryColorAmber));
        themeArrayList.add(new Theme(11,R.color.primaryColorBrown, R.color.primaryDarkColorBrown, R.color.secondaryColorBrown));
        return themeArrayList;
    }

}
