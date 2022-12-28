package tpcreative.co.qrscanner.ui.guides

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.*
import tpcreative.co.qrscanner.R

class GuidesActivity  : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.activity_guides_1))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.activity_guides_2))

//        showStatusBar(false)
//        setStatusBarColorRes(R.color.colorPrimary)
//        setNavBarColorRes(R.color.colorPrimary)
//        setProgressIndicator()
//        setTransformer(AppIntroPageTransformerType.Parallax())
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        finish()
    }
}