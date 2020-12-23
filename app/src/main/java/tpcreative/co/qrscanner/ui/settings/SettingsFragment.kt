package tpcreative.co.qrscanner.ui.settings

import android.R
import android.net.Uri
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import de.mrapp.android.dialog.MaterialDialog
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.Theme
import java.util.*

class SettingsFragment : BaseFragment() {
    protected override fun getLayoutId(): Int {
        return 0
    }

    protected override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater.inflate(R.layout.fragment_settings, viewGroup, false)
    }

    protected override fun work() {
        super.work()
        var fragment: Fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG)
        if (fragment == null) {
            val mFactory: FragmentFactory = getFragmentManager().getFragmentFactory()
            fragment = mFactory.instantiate(ClassLoader.getSystemClassLoader(), SettingsFragmentPreference::class.java.name)
        }
        val transaction: FragmentTransaction = getChildFragmentManager().beginTransaction()
        transaction.replace(R.id.content_frame, fragment)
        transaction.commit()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            QRScannerApplication.Companion.getInstance().getActivity().onShowFloatingButton(this@SettingsFragment, true)
            Utils.Log(TAG, "isVisible")
        } else {
            QRScannerApplication.Companion.getInstance().getActivity().onShowFloatingButton(this@SettingsFragment, false)
            Utils.Log(TAG, "isInVisible")
            HistorySingleton.Companion.getInstance().reloadData()
            SaveSingleton.Companion.getInstance().reloadData()
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

    class SettingsFragmentPreference : PreferenceFragmentCompat(), SingletonSettingsListener {
        var mPosition = Utils.getPositionTheme()
        private var mVersionApp: MyPreference? = null
        private var mPreferencePremiumVersion: MyPreference? = null
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
        private var myPreferenceCategoryFamilyApps: MyPreferenceCategory? = null
        private var bitmap: Bitmap? = null
        override fun onResume() {
            super.onResume()
            SettingsSingleton.Companion.getInstance().setListener(this)
        }

        override fun onUpdated() {
            onGenerateReview("123")
        }

        override fun onSyncDataRequest() {
            if (!Utils.isConnectedToGoogleDrive()) {
                mySwitchPreferenceBackupData.setChecked(false)
            }
        }

        override fun onUpdatedSharePreferences(value: Boolean) {}

        /**
         * Initializes the preference, which allows to change the app's theme.
         */
        fun askPermission() {
            val dialogBuilder = MaterialDialog.Builder(getContext(), Utils.getCurrentTheme())
            dialogBuilder.setTitle(R.string.app_permission)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setCustomMessage(R.layout.custom_body_permission)
            dialogBuilder.setPositiveButton(R.string.got_it, null)
            val dialog = dialogBuilder.create()
            dialogBuilder.setOnShowListener(object : OnShowListener {
                override fun onShow(dialogInterface: DialogInterface?) {
                    val positive = dialog.findViewById<Button?>(R.id.button1)
                    val title: TextView = dialog.findViewById<TextView?>(R.id.title)
                    if (positive != null && title != null) {
                        positive.textSize = 14f
                    }
                }
            })
            dialog.show()
        }

        fun askToDeleteDuplicatesItems(count: Int, listener: ServiceManagerClickedListener?) {
            val dialogBuilder = MaterialDialog.Builder(getContext(), Utils.getCurrentTheme())
            dialogBuilder.setTitle(R.string.alert)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setMessage(kotlin.String.format(getString(R.string.found_items_duplicates), "" + count))
            dialogBuilder.setCancelable(false)
            dialogBuilder.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    listener.onYes()
                }
            })
            dialogBuilder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    mySwitchPreferenceSkipDuplicates.setChecked(false)
                    listener.onNo()
                }
            })
            val dialog = dialogBuilder.create()
            dialog.show()
        }

        fun askChooseTheme(listener: ServiceManagerClickedItemsListener?) {
            val dialogBuilder = MaterialDialog.Builder(getContext(), Utils.getCurrentTheme())
            dialogBuilder.setTitle(R.string.change_theme)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setSingleChoiceItems(R.array.themeEntryArray, Utils.getPositionTheme(), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    mPosition = i
                }
            })
            dialogBuilder.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    Utils.setPositionTheme(EnumThemeMode.Companion.byPosition(mPosition).ordinal)
                    listener.onYes()
                }
            })
            val dialog = dialogBuilder.create()
            dialog.show()
        }

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type [ ]
         */
        private fun createChangeListener(): Preference.OnPreferenceChangeListener? {
            return label@ Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
                if (preference is MySwitchPreference) {
                    if (preference.getKey() == getString(R.string.key_skip_duplicates)) {
                        if (!Utils.isPremium()) {
                            mySwitchPreferenceSkipDuplicates.setChecked(false)
                            Navigator.onMoveProVersion(getContext())
                            return@label false
                        }
                        val mResult = newValue as Boolean
                        if (mResult) {
                            val mSaveList: MutableList<SaveModel?>? = Utils.filterDuplicationsSaveItems(SQLiteHelper.getSaveList())
                            Utils.Log(TAG, "need to be deleted at save " + mSaveList.size)
                            val mHistoryList: MutableList<HistoryModel?>? = Utils.filterDuplicationsHistoryItems(SQLiteHelper.getHistoryList())
                            Utils.Log(TAG, "need to be deleted at history " + mHistoryList.size)
                            val mCount = mSaveList.size + mHistoryList.size
                            if (mCount > 0) {
                                askToDeleteDuplicatesItems(mCount, object : ServiceManagerClickedListener {
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
                    } else if (preference.getKey() == getString(R.string.key_multiple_scan)) {
                        if (!Utils.isPremium()) {
                            myPreferenceMultipleScan.setChecked(false)
                            Navigator.onMoveProVersion(getContext())
                            return@label false
                        }
                    } else if (preference.getKey() == getString(R.string.key_backup_data)) {
                        if (!Utils.isPremium()) {
                            mySwitchPreferenceBackupData.setChecked(false)
                            Navigator.onMoveProVersion(getContext())
                            return@label false
                        }
                        if (Utils.isPremium()) {
                            val mResult = newValue as Boolean
                            if (mResult) {
                                Navigator.onBackupData(getContext())
                            }
                        }
                    }
                }
                true
            }
        }

        fun shareToSocial(value: String?) {
            val intent = Intent()
            intent.setAction(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, value)
            startActivity(Intent.createChooser(intent, "Share"))
        }

        private fun createActionPreferenceClickListener(): Preference.OnPreferenceClickListener? {
            return Preference.OnPreferenceClickListener { preference: Preference? ->
                if (preference is MyPreference) {
                    if (preference.getKey() == getString(R.string.key_app_permissions)) {
                        askPermission()
                    } else if (preference.getKey() == getString(R.string.key_share)) {
                        if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))) {
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
                        Navigator.onMoveToHelp(getContext())
                    } else if (preference.getKey() == getString(R.string.key_color_code)) {
                        if (!Utils.isPremium()) {
                            Navigator.onMoveProVersion(getContext())
                        } else {
                            Navigator.onMoveToChangeFileColor(getActivity())
                        }
                    } else if (preference.getKey() == getString(R.string.key_rate)) {
                        if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))) {
                            onRateProApp()
                        } else {
                            onRateApp()
                        }
                    } else if (preference.getKey() == getString(R.string.key_supersafe)) {
                        onSuperSafe()
                    } else if (preference.getKey() == getString(R.string.key_premium_version)) {
                        Navigator.onMoveProVersion(getContext())
                    } else if (preference.getKey() == getString(R.string.key_dark_mode)) {
                        if (!Utils.isPremium()) {
                            Navigator.onMoveProVersion(getContext())
                        } else {
                            askChooseTheme(ServiceManagerClickedItemsListener {
                                ThemeHelper.applyTheme(EnumThemeMode.Companion.byPosition(Utils.getPositionTheme()))
                                Utils.Log(TAG, "Clicked say yes")
                            })
                        }
                    }
                }
                true
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            /*Version**/mVersionApp = findPreference(getString(R.string.key_version)) as MyPreference?
            mVersionApp.setSummary(java.lang.String.format("Version: %s", BuildConfig.VERSION_NAME))
            mVersionApp.setOnPreferenceChangeListener(createChangeListener())
            mVersionApp.setOnPreferenceClickListener(createActionPreferenceClickListener())

            /*Premium*/mPreferencePremiumVersion = findPreference(getString(R.string.key_premium_version)) as MyPreference?
            mPreferencePremiumVersion.setOnPreferenceChangeListener(createChangeListener())
            mPreferencePremiumVersion.setOnPreferenceClickListener(createActionPreferenceClickListener())
            if (Utils.isPremium()) {
                mPreferencePremiumVersion.setVisible(false)
            }
            /*App Permissions*/myPreferencePermissions = findPreference(getString(R.string.key_app_permissions)) as MyPreference?
            myPreferencePermissions.setOnPreferenceChangeListener(createChangeListener())
            myPreferencePermissions.setOnPreferenceClickListener(createActionPreferenceClickListener())

            /*Share app*/myPreferenceShare = findPreference(getString(R.string.key_share)) as MyPreference?
            myPreferenceShare.setOnPreferenceChangeListener(createChangeListener())
            myPreferenceShare.setOnPreferenceClickListener(createActionPreferenceClickListener())

            /*Rate*/myPreferenceRate = findPreference(getString(R.string.key_rate)) as MyPreference?
            myPreferenceRate.setOnPreferenceChangeListener(createChangeListener())
            myPreferenceRate.setOnPreferenceClickListener(createActionPreferenceClickListener())

            /*Support*/myPreferenceSupport = findPreference(getString(R.string.key_support)) as MyPreference?
            myPreferenceSupport.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceSupport.setOnPreferenceChangeListener(createChangeListener())

            /*Help*/myPreferenceHelp = findPreference(getString(R.string.key_help)) as MyPreference?
            myPreferenceHelp.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceHelp.setOnPreferenceChangeListener(createChangeListener())

            /*Vibrate*/mySwitchPreferenceVibrate = findPreference(getString(R.string.key_vibrate)) as MySwitchPreference?
            mySwitchPreferenceVibrate.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mySwitchPreferenceVibrate.setOnPreferenceChangeListener(createChangeListener())

            /*This area is premium*/

            /*Theme*/myPreferenceTheme = findPreference(getString(R.string.key_dark_mode)) as MyPreference?
            myPreferenceTheme.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceTheme.setOnPreferenceChangeListener(createChangeListener())
            myPreferenceTheme.setListener(object : MyPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceTheme.getTvChoose().setVisibility(View.VISIBLE)
                    myPreferenceTheme.getImgPremium().setVisibility(View.VISIBLE)
                    myPreferenceTheme.getTvChoose().setText(Utils.getCurrentThemeName())
                }
            })
            if (!Utils.isPremium()) {
                myPreferenceTheme.setVisible(false)
            }

            /*File color*/myPreferenceFileColor = findPreference(getString(R.string.key_color_code)) as MyPreference?
            myPreferenceFileColor.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceFileColor.setOnPreferenceChangeListener(createChangeListener())
            myPreferenceFileColor.setListener(object : MyPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceFileColor.getImgPremium().setVisibility(View.VISIBLE)
                    onGenerateReview("123")
                }
            })
            if (!Utils.isPremium()) {
                myPreferenceFileColor.setVisible(false)
            }

            /*Multiple scan*/myPreferenceMultipleScan = findPreference(getString(R.string.key_multiple_scan)) as MySwitchPreference?
            myPreferenceMultipleScan.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceMultipleScan.setOnPreferenceChangeListener(createChangeListener())
            myPreferenceMultipleScan.setListener(object : MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceMultipleScan.getImgPremium().setVisibility(View.VISIBLE)
                }
            })
            if (!Utils.isPremium()) {
                myPreferenceMultipleScan.setVisible(false)
            }

            /*Skip duplicates*/mySwitchPreferenceSkipDuplicates = findPreference(getString(R.string.key_skip_duplicates)) as MySwitchPreference?
            mySwitchPreferenceSkipDuplicates.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mySwitchPreferenceSkipDuplicates.setOnPreferenceChangeListener(createChangeListener())
            mySwitchPreferenceSkipDuplicates.setListener(object : MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    mySwitchPreferenceSkipDuplicates.getImgPremium().setVisibility(View.VISIBLE)
                }
            })
            if (!Utils.isPremium()) {
                mySwitchPreferenceSkipDuplicates.setVisible(false)
            }

            /*Backup data*/mySwitchPreferenceBackupData = findPreference(getString(R.string.key_backup_data)) as MySwitchPreference?
            mySwitchPreferenceBackupData.setOnPreferenceClickListener(createActionPreferenceClickListener())
            mySwitchPreferenceBackupData.setOnPreferenceChangeListener(createChangeListener())
            mySwitchPreferenceBackupData.setListener(object : MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    mySwitchPreferenceBackupData.getImgPremium().setVisibility(View.VISIBLE)
                }
            })
            if (!Utils.isPremium()) {
                mySwitchPreferenceBackupData.setVisible(false)
            }
            myPreferenceCategoryFamilyApps = findPreference(getString(R.string.key_family_apps)) as MyPreferenceCategory?
            myPreferenceCategoryFamilyApps.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceCategoryFamilyApps.setOnPreferenceChangeListener(createChangeListener())

            /*SuperSafe*/myPreferenceSuperSafe = findPreference(getString(R.string.key_supersafe)) as MyPreference?
            myPreferenceSuperSafe.setOnPreferenceClickListener(createActionPreferenceClickListener())
            myPreferenceSuperSafe.setOnPreferenceChangeListener(createChangeListener())
            myPreferenceSuperSafe.setListener(object : MyPreferenceListener {
                override fun onUpdatePreference() {
                    if (myPreferenceSuperSafe.getImgSuperSafe() != null) {
                        myPreferenceSuperSafe.getImgSuperSafe().setImageResource(R.drawable.ic_supersafe_launcher)
                        myPreferenceSuperSafe.getImgSuperSafe().setVisibility(View.VISIBLE)
                    }
                }
            })
            myPreferenceCategoryFamilyApps.setVisible(true)
            myPreferenceSuperSafe.setVisible(true)
        }

        fun onGenerateReview(code: String?) {
            try {
                if (myPreferenceFileColor == null) {
                    if (myPreferenceFileColor.getImageView() == null) {
                        return
                    }
                    return
                }
                val barcodeEncoder = BarcodeEncoder()
                val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
                hints[EncodeHintType.MARGIN] = 2
                val theme: Theme = Theme.Companion.getInstance().getThemeInfo()
                bitmap = barcodeEncoder.encodeBitmap(getContext(), theme.primaryDarkColor, code, BarcodeFormat.QR_CODE, 100, 100, hints)
                myPreferenceFileColor.getImageView().setImageBitmap(bitmap)
                myPreferenceFileColor.getImageView().setVisibility(View.VISIBLE)
                Utils.Log(TAG, "onGenerateReview")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_general)
        }

        fun onSuperSafe() {
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

        fun onRateApp() {
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

        fun onRateProApp() {
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
        private val FRAGMENT_TAG: String? = SettingsFragmentPreference::class.java.simpleName + "::fragmentTag"
        fun newInstance(index: Int): SettingsFragment? {
            val fragment = SettingsFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.setArguments(b)
            return fragment
        }
    }
}