package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.R
import java.util.*

/**
 * Created by Pankaj on 12-11-2017.
 */
object ThemeUtil {
    fun getThemeList(): ArrayList<Theme> {
        val themeArrayList = ArrayList<Theme>()
        themeArrayList.add(Theme(0, R.color.blackTheme, R.color.colorDarkTheme, R.color.colorButton))
        themeArrayList.add(Theme(1, R.color.inbox_primary, R.color.inbox_primary_dark, R.color.colorAccent))
        themeArrayList.add(Theme(2, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton))
        themeArrayList.add(Theme(3, R.color.primaryColorPink, R.color.primaryDarkColorPink, R.color.secondaryColorPink))
        themeArrayList.add(Theme(4, R.color.primaryColorPurple, R.color.primaryDarkColorPurple, R.color.secondaryColorPurple))
        themeArrayList.add(Theme(5, R.color.primaryColorDeepPurple, R.color.primaryDarkColorDeepPurple, R.color.secondaryColorDeepPurple))
        themeArrayList.add(Theme(6, R.color.primaryColorCyan, R.color.primaryDarkColorCyan, R.color.secondaryColorCyan))
        themeArrayList.add(Theme(7, R.color.primaryColorTeal, R.color.primaryDarkColorTeal, R.color.secondaryColorTeal))
        themeArrayList.add(Theme(8, R.color.primaryColorLightGreen, R.color.primaryDarkColorLightGreen, R.color.secondaryColorLightGreen))
        themeArrayList.add(Theme(9, R.color.primaryColorLime, R.color.primaryDarkColorLimeStrong, R.color.secondaryColorLime))
        themeArrayList.add(Theme(10, R.color.primaryColorAmber, R.color.primaryDarkColorAmberStrong, R.color.secondaryColorAmber))
        themeArrayList.add(Theme(11, R.color.primaryColorBrown, R.color.primaryDarkColorBrown, R.color.secondaryColorBrown))
        return themeArrayList
    }
}