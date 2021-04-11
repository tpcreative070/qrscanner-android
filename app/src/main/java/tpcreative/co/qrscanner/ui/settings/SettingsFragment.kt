package tpcreative.co.qrscanner.ui.settings
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import co.tpcreative.supersafe.common.controller.EncryptedPreferenceDataStore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.mrapp.android.dialog.MaterialDialog
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
    private  var isPremium = Utils.isPremium()
    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(R.layout.fragment_settings, viewGroup, false)
    }

    override fun work() {
        super.work()
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
            QRScannerApplication.getInstance().getActivity()?.onShowFloatingButton(this@SettingsFragment, true)
            Utils.Log(TAG, "isVisible")
        } else {
            QRScannerApplication.getInstance().getActivity()?.onShowFloatingButton(this@SettingsFragment, false)
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
        if (isPremium != Utils.isPremium()){
            work()
            isPremium = Utils.isPremium()
        }
    }

    class SettingsFragmentPreference : PreferenceFragmentCompat(), SettingsSingleton.SingletonSettingsListener {
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
            val dialogBuilder = MaterialDialog.Builder(context!!, Utils.getCurrentTheme())
            dialogBuilder.setTitle(R.string.app_permission)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setCustomMessage(R.layout.custom_body_permission)
            dialogBuilder.setPositiveButton(R.string.got_it, null)
            val dialog = dialogBuilder.create()
            dialogBuilder.setOnShowListener {
                val positive = dialog.findViewById<Button?>(android.R.id.button1)
                val title: TextView = dialog.findViewById<TextView?>(R.id.title)
                if (positive != null && title != null) {
                    positive.textSize = 14f
                }
            }
            dialog.show()
        }

        private fun askToDeleteDuplicatesItems(count: Int, listener: ServiceManager.ServiceManagerClickedListener?) {
            val dialogBuilder = MaterialDialog.Builder(context!!, Utils.getCurrentTheme())
            dialogBuilder.setTitle(R.string.alert)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setMessage(kotlin.String.format(getString(R.string.found_items_duplicates), "" + count))
            dialogBuilder.setCancelable(false)
            dialogBuilder.setPositiveButton(R.string.yes) { dialogInterface, i -> listener?.onYes() }
            dialogBuilder.setNegativeButton(R.string.no) { dialogInterface, i ->
                mySwitchPreferenceSkipDuplicates?.isChecked = false
                listener?.onNo()
            }
            val dialog = dialogBuilder.create()
            dialog.show()
        }

        private fun askChooseTheme(listener: ServiceManager.ServiceManagerClickedItemsListener?) {
            val dialogBuilder = MaterialDialog.Builder(context!!, Utils.getCurrentTheme())
            dialogBuilder.setTitle(R.string.change_theme)
            dialogBuilder.setPadding(40, 40, 40, 0)
            dialogBuilder.setMargin(60, 0, 60, 0)
            dialogBuilder.setSingleChoiceItems(R.array.themeEntryArray, Utils.getPositionTheme(), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    mPosition = i
                }
            })
            dialogBuilder.setPositiveButton(R.string.yes) { _, i ->
                EnumThemeMode.byPosition(mPosition)?.ordinal?.let { Utils.setPositionTheme(it) }
                listener?.onYes()
            }
            val dialog = dialogBuilder.create()
            dialog.show()
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
                        if (!Utils.isPremium()) {
                            mySwitchPreferenceSkipDuplicates?.isChecked = false
                            Navigator.onMoveProVersion(context)
                            return@OnPreferenceChangeListener false
                        }
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
                    } else if (preference.getKey() == getString(R.string.key_multiple_scan)) {
                        if (!Utils.isPremium()) {
                            myPreferenceMultipleScan?.isChecked = false
                            Navigator.onMoveProVersion(context)
                            return@OnPreferenceChangeListener false
                        }
                    } else if (preference.getKey() == getString(R.string.key_backup_data)) {
                        if (!Utils.isPremium()) {
                            mySwitchPreferenceBackupData?.isChecked = false
                            Navigator.onMoveProVersion(context)
                            return@OnPreferenceChangeListener false
                        }
                        if (Utils.isPremium()) {
                            val mResult = newValue as Boolean
                            if (mResult) {
                                Navigator.onBackupData(context)
                            }
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
                        if (!Utils.isPremium()) {
                            Navigator.onMoveProVersion(context)
                        } else {
                            Navigator.onMoveToChangeFileColor(activity)
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
                        Navigator.onMoveProVersion(context)
                    } else if (preference.getKey() == getString(R.string.key_dark_mode)) {
                        if (!Utils.isPremium()) {
                            Navigator.onMoveProVersion(context)
                        } else {
                            askChooseTheme(object : ServiceManager.ServiceManagerClickedItemsListener {
                                override fun onYes() {
                                    EnumThemeMode.byPosition(Utils.getPositionTheme())?.let { ThemeHelper.applyTheme(it) }
                                    Utils.Log(TAG, "Clicked say yes")
                                }
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
            mVersionApp?.summary = java.lang.String.format("Version: %s", BuildConfig.VERSION_NAME)
            mVersionApp?.onPreferenceChangeListener = createChangeListener()
            mVersionApp?.onPreferenceClickListener = createActionPreferenceClickListener()

            /*Premium*/mPreferencePremiumVersion = findPreference(getString(R.string.key_premium_version)) as MyPreference?
            mPreferencePremiumVersion?.onPreferenceChangeListener = createChangeListener()
            mPreferencePremiumVersion?.onPreferenceClickListener = createActionPreferenceClickListener()
            if (Utils.isPremium()) {
                mPreferencePremiumVersion?.isVisible = false
            }
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
                    myPreferenceTheme?.getImgPremium()?.visibility = View.VISIBLE
                    myPreferenceTheme?.getTvChoose()?.text = Utils.getCurrentThemeName()
                }
            })
            if (!Utils.isPremium()) {
                myPreferenceTheme?.isVisible = false
            }

            /*File color*/myPreferenceFileColor = findPreference(getString(R.string.key_color_code)) as MyPreference?
            myPreferenceFileColor?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceFileColor?.onPreferenceChangeListener = createChangeListener()
            myPreferenceFileColor?.setListener(object : MyPreference.MyPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceFileColor?.getImgPremium()?.visibility = View.VISIBLE
                    onGenerateReview("123")
                }
            })
            if (!Utils.isPremium()) {
                myPreferenceFileColor?.isVisible = false
            }

            /*Multiple scan*/myPreferenceMultipleScan = findPreference(getString(R.string.key_multiple_scan)) as MySwitchPreference?
            myPreferenceMultipleScan?.onPreferenceClickListener = createActionPreferenceClickListener()
            myPreferenceMultipleScan?.onPreferenceChangeListener = createChangeListener()
            myPreferenceMultipleScan?.setListener(object : MySwitchPreference.MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    myPreferenceMultipleScan?.getImgPremium()?.visibility = View.VISIBLE
                }
            })
            if (!Utils.isPremium()) {
                myPreferenceMultipleScan?.isVisible = false
            }

            /*Skip duplicates*/mySwitchPreferenceSkipDuplicates = findPreference(getString(R.string.key_skip_duplicates)) as MySwitchPreference?
            mySwitchPreferenceSkipDuplicates?.onPreferenceClickListener = createActionPreferenceClickListener()
            mySwitchPreferenceSkipDuplicates?.onPreferenceChangeListener = createChangeListener()
            mySwitchPreferenceSkipDuplicates?.setListener(object : MySwitchPreference.MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    mySwitchPreferenceSkipDuplicates?.getImgPremium()?.visibility = View.VISIBLE
                }
            })
            if (!Utils.isPremium()) {
                mySwitchPreferenceSkipDuplicates?.isVisible = false
            }

            /*Backup data*/mySwitchPreferenceBackupData = findPreference(getString(R.string.key_backup_data)) as MySwitchPreference?
            mySwitchPreferenceBackupData?.onPreferenceClickListener = createActionPreferenceClickListener()
            mySwitchPreferenceBackupData?.onPreferenceChangeListener = createChangeListener()
            mySwitchPreferenceBackupData?.setListener(object : MySwitchPreference.MySwitchPreferenceListener {
                override fun onUpdatePreference() {
                    mySwitchPreferenceBackupData?.getImgPremium()?.visibility = View.VISIBLE
                }
            })
            if (!Utils.isPremium()) {
                mySwitchPreferenceBackupData?.isVisible = false
            }
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
            myPreferenceCategoryFamilyApps?.isVisible = true
            myPreferenceSuperSafe?.isVisible = true
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
                bitmap = barcodeEncoder.encodeBitmap(context, theme?.getPrimaryDarkColor()!!, code, BarcodeFormat.QR_CODE, 100, 100, hints)
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