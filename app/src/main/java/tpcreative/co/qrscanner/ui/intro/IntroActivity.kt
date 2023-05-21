package tpcreative.co.qrscanner.ui.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils

class IntroActivity  : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.activity_intro_1))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.activity_intro_2))
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.activity_intro_3))
        Utils.onIntro(true)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        Navigator.onMoveMainTab(this)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        Navigator.onMoveMainTab(this)
        finish()
    }
}