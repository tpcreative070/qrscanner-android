package tpcreative.co.qrscanner.ui.settings
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.tpcreative.supersafe.common.controller.EncryptedPreferenceDataStore
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.instantiate
import tpcreative.co.qrscanner.common.preference.MyPreference
import tpcreative.co.qrscanner.common.preference.MyPreferenceCategory
import tpcreative.co.qrscanner.common.preference.MySwitchPreference
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.helper.ThemeHelper
import tpcreative.co.qrscanner.model.EnumThemeMode
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.model.SaveModel
import tpcreative.co.qrscanner.model.Theme
import java.util.*

class SettingsFragment : BaseFragment() {
    private var mStateSaved = false

    override fun onSaveInstanceState(outState: Bundle) {
        mStateSaved = true;
        super.onSaveInstanceState(outState)
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(R.layout.fragment_settings, viewGroup, false)
    }

    override fun work() {
        super.work()
        //Avoid crack app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity?.supportFragmentManager?.isStateSaved == true) {
                return;
            }
        }
        if (mStateSaved) {
            return
        }
        val fragment: Fragment? = activity?.supportFragmentManager?.instantiate(SettingsFragmentPreference::class.java.name)
        val transaction: FragmentTransaction? = activity?.supportFragmentManager?.beginTransaction()
        fragment?.let {
            transaction?.replace(R.id.content_frame, it)
            transaction?.commit()
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            QRScannerApplication.getInstance().getActivity()?.onVisitableFragment()
            Utils.Log(TAG, "isVisible")
        } else {
            QRScannerApplication.getInstance().getActivity()?.onVisitableFragment()
            Utils.Log(TAG, "isInVisible")
            HistorySingleton.getInstance()?.reloadData()
            SaveSingleton.getInstance()?.reloadData()
        }
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
    }

    class SettingsFragmentPreference : PreferenceFragmentCompat(), SettingsSingleton.SingletonSettingsListener {
        var mPosition = Utils.getPositionTheme()
        private var mVersionApp: MyPreference? = null
        private var myPreferenceShare: MyPreference? = null
        private var myPreferencePermissions: MyPreference? = null
        private var myPreferenceRate: MyPreference? = null
        private var myPreferenceSupport: MyPreference? = null
        private var myPreferenceHelp: MyPreference? = null
        private var mySwitchPreferenceVibrate: MySwitchPreference? = null
        private var mySwitchPreferenceBackupData: MySwitchPreference? = null
        private var myPreferenceTheme: MyPreference? = null
        private var myPreferenceFileColor: MyPreference? = null
        private var myPreferenceMultipleScan: MySwitchPreference? = null
        private var mySwitchPreferenceSkipDuplicates: MySwitchPreference? = null
        private var myPreferenceSuperSafe: MyPreference? = null
        private var myPreferenceSaveYourVoicemails: MyPreference? = null
        private var myPreferenceCategoryFamilyApps: MyPreferenceCategory? = null
        private var bitmap: Bitmap? = null
        override fun onResume() {
            super.onResume()
            SettingsSingleton.getInstance()?.setListener(this)
        }

        override fun onUpdated() {
            onGenerateReview("123")
        }

        override fun onSyncDataRequest() {
            if (!Utils.isConnectedToGoogleDrive()) {
                mySwitchPreferenceBackupData?.isChecked = false
            }
        }

        override fun onUpdatedSharePreferences(value: Boolean) {
        }

        /**
         * Initializes the preference, which allows to change the app's theme.
         */
        private fun askPermission() {
            MaterialDialog(requireContext()).show {
                title(R.string.app_permission)
                customView(R.layout.custom_body_permission)
                positiveButton(R.string.ok)
            }
        }

        private fun askToDeleteDuplicatesItems(count: Int, listener: ServiceManager.ServiceManagerClickedListener?) {
            MaterialDialog(requireContext()).show {
                title(R.string.alert)
                message(text = kotlin.String.format(getString(R.string.found_items_duplicates), "" + count))
                positiveButton(R.string.yes){
                    listener?.onYes()
                }
                negativeButton (R.string.no){
                    mySwitchPreferenceSkipDuplicates?.isChecked = false
                    listener?.onNo()
                }
            }
        }

        private fun askChooseTheme(listener: ServiceManager.ServiceManagerClickedItemsListener?) {
            MaterialDialog(requireContext()).show {
                title(R.string.change_theme)
                listItemsSingleChoice(R.array.themeEntryArray, initialSelection = Utils.getPositionTheme()) { dialog, index, text ->
                    mPosition =index
                    EnumThemeMode.byPosition(mPosition)?.ordinal?.let { Utils.setPositionTheme(it) }
                    listener?.onYes()
                }.positiveButton(R.string.yes){
                }
                negativeButton (R.string.no){
                }
            }
        }

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type [ ]
         */
        private fun createChangeListener(): Preference.OnPreferenceChangeListener {
            return Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if (preference is MySwitchPreference) {
                    if (preference.getKey() == getString(R.string.key_skip_duplicates)) {
                        val mResult = newValue as Boolean
                        if (mResult) {
                            val mSaveList: MutableList<SaveModel> = Utils.filterDuplicationsSaveItems(SQLiteHelper.getSaveList())
                            Utils.Log(TAG, "need to be deleted at save " + mSaveList.size)
                            val mHistoryList: MutableList<HistoryModel> = Utils.filterDuplicationsHistoryItems(SQLiteHelper.getHistoryList())
                            Utils.Log(TAG, "need to be deleted at history " + mHistoryList.size)
                            val mCount = mSaveList.size + mHistoryList.size
                            if (mCount > 0) {
                                askToDeleteDuplicatesItems(mCount, object : ServiceManager.ServiceManagerClickedListener {
                                    override fun onYes() {
                                        for (index in mSaveList) {
                                            SQLiteHelper.onDelete(index)
                                        }
                                        for (index in mHistoryList) {
                                            SQLiteHelper.onDelete(index)
                                        }
                                    }

                                    override fun onNo() {}
                                })
                            }
                        }
                        Utils.Log(TAG, "CLicked $newValue")
                    } else if (preference.getKey() == getString(R.string.key_backup_data)) {
                        val mResult = newValue as Boolean
                        if (mResult) {
                            Navigator.onBackupData(context)
                        }
                    }
                }
                true
            }
        }

        private fun shareToSocial(value: String?) {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, value)
            startActivity(Intent.createChooser(intent, "Share"))
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener {
            return Preference.OnPreferenceClickListener { preference: Preference? ->
                if (preference is MyPreference) {
                    if (preference.getKey() == getString(R.string.key_app_permissions)) {
                        askPermission()
                    } else if (preference.getKey() == getString(R.string.key_share)) {
                        if (BuildConfig.APPLICATION_ID == getString(R.string.qrscanner_pro_release)) {
                            shareToSocial(getString(R.string.scanner_app_pro))
                        } else {
                            shareToSocial(getString(R.string.scanner_app))
                        }
                    } else if (preference.getKey() == getString(R.string.key_support)) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "care@tpcreative.me"))
                            intent.putExtra(Intent.EXTRA_SUBJECT, "QRScanner App Support")
                            intent.putExtra(Intent.EXTRA_TEXT, "")
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            //TODO smth
                        }
                    } else if (preference.getKey() == getString(R.string.key_help)) {
                        Utils.Log(TAG, "action here")
                        Navigator.onMoveToHelp(context)
                    } else if (preference.getKey() == getString(R.string.key_color_code)) {
                        Navigator.onMoveToChangeFileColor(activity)
                    } else if (preference.getKey() == getString(R.string.key_rate)) {
                        if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))) {
                            onRateProApp()
                        } else {
                            onRateApp()
                        }
                    } else if (preference.getKey() == getString(R.string.key_supersafe)) {
                        onSuperSafe()
                    }
                    else if (preference.getKey() == getString(R.string.key_save_your_voicemails)){
                        onSaveYourVoicemails()
                    }
                    else if (preference.getKey() == getString(R.string.key_dark_mode)) {
                        askChooseTheme(object : ServiceManager.ServiceManagerClickedItemsListener {
                            override fun onYes() {
                                EnumThemeMode.byPosition(Utils.getPositionTheme())?.let { ThemeHelper.applyTheme(it) }
                                Utils.Log(TAG, "Clicked say yes")
                            }
                        })
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*Version**/mVersionApp = findPreference(getString(R.string.key_version)) as MyPreference?
            mVersionApp?.summary = java.lang.String.format("V: %s", BuildConfig.VERSION_NAME)
            mVersionApp?.onPreferenceChangeListener = createChangeListener()
            mVersionApp?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*App Permissions*/myPreferencePermissions = findPreference(getString(R.string.key_app_permissions)) as MyPreference?
            myPreferencePermissions?.onPreferenceChangeListener = createChangeListener()
            myPreferencePermissions?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*Share app*/myPreferenceShare = findPreference(getString(R.string.key_share)) as MyPreference?
            myPreferenceShare?.onPreferenceChangeListener = createChangeListener()
            myPreferenceShare?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*Rate*/myPreferenceRate = findPreference(getString(R.string.key_rate)) as MyPreference?
            myPreferenceRate?.onPreferenceChangeListener = createChangeListener()
            myPreferenceRate?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*Support*/myPreferenceSupport = findPreference(getString(R.string.key_support)) as MyPreference?
            myPreferenceSupport?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceSupport?.onPreferenceChangeListener = createChangeListener()

            /*Help*/myPreferenceHelp = findPreference(getString(R.string.key_help)) as MyPreference?
            myPreferenceHelp?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceHelp?.onPreferenceChangeListener = createChangeListener()

            /*Vibrate*/mySwitchPreferenceVibrate = findPreference(getString(R.string.key_vibrate)) as MySwitchPreference?
            mySwitchPreferenceVibrate?.onPreferenceClickListener = createActionPreferenceClickListener()
            mySwitchPreferenceVibrate?.onPreferenceChangeListener = createChangeListener()

            /*This area is premium*/

            /*Theme*/myPreferenceTheme = findPreference(getString(R.string.key_dark_mode)) as MyPreference?
            myPreferenceTheme?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceTheme?.onPreferenceChangeListener = createChangeListener()
            myPreferenceTheme?.setListener(object : MyPreference.MyPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceTheme?.getTvChoose()?.visibility = View.VISIBLE
                    myPreferenceTheme?.getImgPremium()?.visibility = View.INVISIBLE
                    myPreferenceTheme?.getTvChoose()?.text = Utils.getCurrentThemeName()
                }
            })

            /*File color*/myPreferenceFileColor = findPreference(getString(R.string.key_color_code)) as MyPreference?
            myPreferenceFileColor?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceFileColor?.onPreferenceChangeListener = createChangeListener()
            myPreferenceFileColor?.setListener(object : MyPreference.MyPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceFileColor?.getImgPremium()?.visibility = View.INVISIBLE
                    onGenerateReview("123")
                }
            })

            /*Multiple scan*/myPreferenceMultipleScan = findPreference(getString(R.string.key_multiple_scan)) as MySwitchPreference?
            myPreferenceMultipleScan?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceMultipleScan?.onPreferenceChangeListener = createChangeListener()
            myPreferenceMultipleScan?.setListener(object : MySwitchPreference.MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceMultipleScan?.getImgPremium()?.visibility = View.INVISIBLE
                }
            })

            /*Skip duplicates*/mySwitchPreferenceSkipDuplicates = findPreference(getString(R.string.key_skip_duplicates)) as MySwitchPreference?
            mySwitchPreferenceSkipDuplicates?.onPreferenceClickListener = createActionPreferenceClickListener()
            mySwitchPreferenceSkipDuplicates?.onPreferenceChangeListener = createChangeListener()
            mySwitchPreferenceSkipDuplicates?.setListener(object : MySwitchPreference.MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    mySwitchPreferenceSkipDuplicates?.getImgPremium()?.visibility = View.INVISIBLE
                }
            })

            /*Backup data*/mySwitchPreferenceBackupData = findPreference(getString(R.string.key_backup_data)) as MySwitchPreference?
            mySwitchPreferenceBackupData?.onPreferenceClickListener = createActionPreferenceClickListener()
            mySwitchPreferenceBackupData?.onPreferenceChangeListener = createChangeListener()
            mySwitchPreferenceBackupData?.setListener(object : MySwitchPreference.MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    mySwitchPreferenceBackupData?.getImgPremium()?.visibility = View.INVISIBLE
                }
            })

            myPreferenceCategoryFamilyApps = findPreference(getString(R.string.key_family_apps)) as MyPreferenceCategory?
            myPreferenceCategoryFamilyApps?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceCategoryFamilyApps?.onPreferenceChangeListener = createChangeListener()

            /*SuperSafe*/myPreferenceSuperSafe = findPreference(getString(R.string.key_supersafe)) as MyPreference?
            myPreferenceSuperSafe?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceSuperSafe?.onPreferenceChangeListener = createChangeListener()
            myPreferenceSuperSafe?.setListener(object : MyPreference.MyPreferenceListener {
                override fun onUpdatePreference() {
                    if (myPreferenceSuperSafe?.getImgSuperSafe() != null) {
                        myPreferenceSuperSafe?.getImgSuperSafe()?.setImageResource(R.drawable.ic_supersafe_launcher)
                        myPreferenceSuperSafe?.getImgSuperSafe()?.visibility = View.VISIBLE
                    }
                }
            })
            myPreferenceSuperSafe?.isVisible = false

            /*SuperSafe*/
            myPreferenceSaveYourVoicemails = findPreference(getString(R.string.key_save_your_voicemails)) as MyPreference?
            myPreferenceSaveYourVoicemails?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceSaveYourVoicemails?.onPreferenceChangeListener = createChangeListener()
            myPreferenceSaveYourVoicemails?.setListener(object : MyPreference.MyPreferenceListener {
                override fun onUpdatePreference() {
                    if (myPreferenceSaveYourVoicemails?.getImgSuperSafe() != null) {
                        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_saveyourvoicemails)
                        val rounded = RoundedBitmapDrawableFactory.create(resources, bitmap)
                        rounded.cornerRadius = 50f
                        myPreferenceSaveYourVoicemails?.getImgSuperSafe()?.setImageDrawable(rounded)
                        myPreferenceSaveYourVoicemails?.getImgSuperSafe()?.visibility = View.VISIBLE
                    }
                }
            })
            myPreferenceSaveYourVoicemails?.isVisible = false

            myPreferenceCategoryFamilyApps?.isVisible = false
        }

        fun onGenerateReview(code: String?) {
            try {
                if (myPreferenceFileColor == null) {
                    if (myPreferenceFileColor?.getImageView() == null) {
                        return
                    }
                    return
                }
                val barcodeEncoder = BarcodeEncoder()
                val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
                hints[EncodeHintType.MARGIN] = 2
                val theme: Theme? = Theme.getInstance()?.getThemeInfo()
                bitmap = barcodeEncoder.encodeBitmap(context, theme?.getPrimaryDarkColor()!!, code, BarcodeFormat.QR_CODE, Constant.QRCodeViewWidth,Constant.QRCodeViewHeight, hints)
                myPreferenceFileColor?.getImageView()?.setImageBitmap(bitmap)
                myPreferenceFileColor?.getImageView()?.visibility = View.VISIBLE
                Utils.Log(TAG, "onGenerateReview")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            if (QRScannerApplication.getInstance().isLiveMigration()){
                preferenceManager.preferenceDataStore = EncryptedPreferenceDataStore.getInstance(requireContext())
            }
            addPreferencesFromResource(R.xml.pref_general)
        }

        private fun onSuperSafe() {
            val uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live))
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))))
            }
        }

        private fun onSaveYourVoicemails() {
            val uri = Uri.parse("market://details?id=" + getString(R.string.save_your_voicemails_live))
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))))
            }
        }

        private fun onRateApp() {
            val uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_free_release))
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_free_release))))
            }
        }

        private fun onRateProApp() {
            val uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_pro_release))
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_pro_release))))
            }
        }
    }

    companion object {
        private val TAG = SettingsFragment::class.java.simpleName
        fun newInstance(index: Int): SettingsFragment {
            val fragment = SettingsFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}