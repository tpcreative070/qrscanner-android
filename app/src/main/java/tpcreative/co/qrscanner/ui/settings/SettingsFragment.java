package tpcreative.co.qrscanner.ui.settings;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.BaseFragment;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.HistorySingleton;
import tpcreative.co.qrscanner.common.SaveSingleton;
import tpcreative.co.qrscanner.common.SettingsSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.preference.MyPreference;
import tpcreative.co.qrscanner.common.preference.MyPreferenceCategory;
import tpcreative.co.qrscanner.common.preference.MySwitchPreference;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.helper.ThemeHelper;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.EnumThemeMode;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveModel;
import tpcreative.co.qrscanner.model.Theme;

public class SettingsFragment extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsFragmentPreference.class.getSimpleName() + "::fragmentTag";

    public static SettingsFragment newInstance(int index) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.fragment_settings, viewGroup, false);
        return view;
    }

    @Override
    protected void work() {
        super.work();
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            final  FragmentFactory mFactory = getFragmentManager().getFragmentFactory();
            fragment = mFactory.instantiate(ClassLoader.getSystemClassLoader(),SettingsFragmentPreference.class.getName());
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(SettingsFragment.this,true);
            Utils.Log(TAG, "isVisible");
        } else {
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(SettingsFragment.this,false);
            Utils.Log(TAG, "isInVisible");
            HistorySingleton.getInstance().reLoadData();
            SaveSingleton.getInstance().reLoadData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG, "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.Log(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG, "onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG, "onResume");
    }

    public static class SettingsFragmentPreference extends PreferenceFragmentCompat implements SettingsSingleton.SingletonSettingsListener{
        int mPosition = Utils.getPositionTheme();
        private MyPreference mVersionApp;

        private MyPreference mPreferencePremiumVersion;

        private MyPreference myPreferenceShare;

        private MyPreference myPreferencePermissions;

        private MyPreference myPreferenceRate;

        private MyPreference myPreferenceSupport;

        private MyPreference myPreferenceHelp;

        private MySwitchPreference mySwitchPreferenceVibrate;

        private MySwitchPreference mySwitchPreferenceBackupData;

        private MyPreference myPreferenceTheme;

        private MyPreference myPreferenceFileColor;

        private MySwitchPreference myPreferenceMultipleScan;

        private MySwitchPreference mySwitchPreferenceSkipDuplicates;

        private MyPreference myPreferenceSuperSafe;

        private MyPreferenceCategory myPreferenceCategoryFamilyApps;


        private Bitmap bitmap;


        @Override
        public void onResume() {
            super.onResume();
            SettingsSingleton.getInstance().setListener(this);
        }

        @Override
        public void onUpdated() {
            onGenerateReview("123");
        }

        @Override
        public void onUpdatedSharePreferences(boolean value) {

        }

        /**
         * Initializes the preference, which allows to change the app's theme.
         */

        public void askPermission() {
            PrefsController.putBoolean(getString(R.string.key_already_load_app), true);
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext(),Utils.getCurrentTheme());
            dialogBuilder.setTitle(R.string.app_permission);
            dialogBuilder.setPadding(40,40,40,0);
            dialogBuilder.setMargin(60,0,60,0);
            dialogBuilder.setCustomMessage(R.layout.custom_body_permission);
            dialogBuilder.setPositiveButton(R.string.got_it, null);
            MaterialDialog dialog = dialogBuilder.create();
            dialogBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positive = dialog.findViewById(android.R.id.button1);
                    TextView title = dialog.findViewById(android.R.id.title);
                    if (positive!=null && title!=null){
                        title.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.black));
                        positive.setTextSize(14);
                    }
                }
            });
            dialog.show();
        }

        public void askToDeleteDuplicatesItems(int count, ServiceManager.ServiceManagerClickedListener listener) {
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext(),Utils.getCurrentTheme());
            dialogBuilder.setTitle(R.string.alert);
            dialogBuilder.setPadding(40,40,40,0);
            dialogBuilder.setMargin(60,0,60,0);
            dialogBuilder.setMessage(String.format(getString(R.string.found_items_duplicates),""+count));
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onYes();
                }
            });
            dialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mySwitchPreferenceSkipDuplicates.setChecked(false);
                    listener.onNo();
                }
            });
            MaterialDialog dialog = dialogBuilder.create();
            dialog.show();
        }

        public void askChooseTheme(ServiceManager.ServiceManagerClickedItemsListener listener) {
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext(),Utils.getCurrentTheme());
            dialogBuilder.setTitle(R.string.change_theme);
            dialogBuilder.setPadding(40,40,40,0);
            dialogBuilder.setMargin(60,0,60,0);
            dialogBuilder.setSingleChoiceItems(R.array.themeEntryArray, Utils.getPositionTheme(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface,int i) {
                    mPosition = i;
                }
            });
            dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Utils.setPositionTheme(EnumThemeMode.byPosition(mPosition).ordinal());
                    listener.onYes();
                }
            });
            MaterialDialog dialog = dialogBuilder.create();
            dialog.show();
        }

        /**
         * Creates and returns a listener, which allows to adapt the app's theme, when the value of the
         * corresponding preference has been changed.
         *
         * @return The listener, which has been created, as an instance of the type {@link
         * Preference.OnPreferenceChangeListener}
         */

        private Preference.OnPreferenceChangeListener createChangeListener() {
            return new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                    if (preference instanceof MySwitchPreference) {
                        if (preference.getKey().equals(getString(R.string.key_skip_duplicates))){
                            if (!Utils.isPremium()){
                                mySwitchPreferenceSkipDuplicates.setChecked(false);
                                Navigator.onMoveProVersion(getContext());
                                return false;
                            }
                            final boolean mResult = (boolean)newValue;
                            if (mResult){
                                final List<SaveModel> mSaveList = Utils.filterDuplicationsSaveItems(SQLiteHelper.getSaveList());
                                Utils.Log(TAG,"need to be deleted at save "+mSaveList.size());
                                final List<HistoryModel> mHistoryList = Utils.filterDuplicationsHistoryItems(SQLiteHelper.getHistoryList());
                                Utils.Log(TAG,"need to be deleted at history "+mHistoryList.size());
                                final int mCount = mSaveList.size() + mHistoryList.size();
                                if (mCount>0){
                                    askToDeleteDuplicatesItems(mCount, new ServiceManager.ServiceManagerClickedListener() {
                                        @Override
                                        public void onYes() {
                                            for (SaveModel index : mSaveList){
                                                SQLiteHelper.onDelete(index);
                                            }
                                            for (HistoryModel index : mHistoryList){
                                                SQLiteHelper.onDelete(index);
                                            }

                                        }
                                        @Override
                                        public void onNo() {

                                        }
                                    });
                                }
                            }
                           Utils.Log(TAG,"CLicked "+ newValue);
                        }
                        else if (preference.getKey().equals(getString(R.string.key_multiple_scan))){
                            if (!Utils.isPremium()){
                                myPreferenceMultipleScan.setChecked(false);
                                Navigator.onMoveProVersion(getContext());
                                return  false;
                            }
                        }
                        else if (preference.getKey().equals(getString(R.string.key_backup_data))){
                            if (!Utils.isPremium()){
                                mySwitchPreferenceBackupData.setChecked(false);
                                Navigator.onMoveProVersion(getContext());
                                return false;
                            }
                            if (Utils.isPremium()){
                                final boolean mResult = (boolean)newValue;
                                if (mResult) {
                                    if (!Utils.isConnectedToGoogleDrive()) {
                                        Navigator.onBackupData(getContext());
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
            };
        }

        public void shareToSocial(String value) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, value);
            startActivity(Intent.createChooser(intent, "Share"));
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (preference instanceof MyPreference) {
                        if (preference.getKey().equals(getString(R.string.key_app_permissions))) {
                            askPermission();
                        } else if (preference.getKey().equals(getString(R.string.key_share))) {
                            if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))){
                                shareToSocial(getString(R.string.scanner_app_pro));
                            }else{
                                shareToSocial(getString(R.string.scanner_app));
                            }
                        } else if (preference.getKey().equals(getString(R.string.key_support))) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "care@tpcreative.me"));
                                intent.putExtra(Intent.EXTRA_SUBJECT, "QRScanner App Support");
                                intent.putExtra(Intent.EXTRA_TEXT, "");
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                //TODO smth
                            }
                        } else if (preference.getKey().equals(getString(R.string.key_help))) {
                            Utils.Log(TAG, "action here");
                            Navigator.onMoveToHelp(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_color_code))){
                            if (!Utils.isPremium()){
                                Navigator.onMoveProVersion(getContext());
                            }else{
                                Navigator.onMoveToChangeFileColor(getActivity());
                            }
                        }
                        else if (preference.getKey().equals(getString(R.string.key_rate))){
                            if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))){
                                onRateProApp();
                            }else{
                                onRateApp();
                            }
                        }
                        else if (preference.getKey().equals(getString(R.string.key_supersafe))){
                            onSuperSafe();
                        }
                        else if (preference.getKey().equals(getString(R.string.key_premium_version))){
                            Navigator.onMoveProVersion(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_theme))){
                            if (!Utils.isPremium()){
                                Navigator.onMoveProVersion(getContext());
                            }else {
                                askChooseTheme(new ServiceManager.ServiceManagerClickedItemsListener() {
                                    @Override
                                    public void onYes() {
                                        ThemeHelper.applyTheme(EnumThemeMode.byPosition(Utils.getPositionTheme()));
                                        Utils.Log(TAG, "Clicked say yes");
                                    }
                                });
                            }
                        }
                    }
                    return true;
                }
            };
        }

        @Override
        public final void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*Version**/
            mVersionApp = (MyPreference) findPreference(getString(R.string.key_version));
            mVersionApp.setSummary(String.format("Version: %s", BuildConfig.VERSION_NAME));
            mVersionApp.setOnPreferenceChangeListener(createChangeListener());
            mVersionApp.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Premium*/
            mPreferencePremiumVersion = (MyPreference) findPreference(getString(R.string.key_premium_version));
            mPreferencePremiumVersion.setOnPreferenceChangeListener(createChangeListener());
            mPreferencePremiumVersion.setOnPreferenceClickListener(createActionPreferenceClickListener());
            if (Utils.isPremium()){
                mPreferencePremiumVersion.setVisible(false);
            }
            /*App Permissions*/
            myPreferencePermissions = (MyPreference) findPreference(getString(R.string.key_app_permissions));
            myPreferencePermissions.setOnPreferenceChangeListener(createChangeListener());
            myPreferencePermissions.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Share app*/
            myPreferenceShare = (MyPreference) findPreference(getString(R.string.key_share));
            myPreferenceShare.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceShare.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Rate*/
            myPreferenceRate = (MyPreference) findPreference(getString(R.string.key_rate));
            myPreferenceRate.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceRate.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Support*/
            myPreferenceSupport = (MyPreference) findPreference(getString(R.string.key_support));
            myPreferenceSupport.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceSupport.setOnPreferenceChangeListener(createChangeListener());

            /*Help*/
            myPreferenceHelp = (MyPreference) findPreference(getString(R.string.key_help));
            myPreferenceHelp.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceHelp.setOnPreferenceChangeListener(createChangeListener());

            /*Vibrate*/
            mySwitchPreferenceVibrate = (MySwitchPreference) findPreference(getString(R.string.key_vibrate));
            mySwitchPreferenceVibrate.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mySwitchPreferenceVibrate.setOnPreferenceChangeListener(createChangeListener());

            /*This area is premium*/

            /*Theme*/
            myPreferenceTheme = (MyPreference) findPreference(getString(R.string.key_theme));
            myPreferenceTheme.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceTheme.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceTheme.setListener(new MyPreference.MyPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    myPreferenceTheme.getTvChoose().setVisibility(View.VISIBLE);
                    myPreferenceTheme.getImgPremium().setVisibility(View.VISIBLE);
                    myPreferenceTheme.getTvChoose().setText(Utils.getCurrentThemeName());
                }
            });

            /*File color*/
            myPreferenceFileColor = (MyPreference) findPreference(getString(R.string.key_color_code));
            myPreferenceFileColor.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceFileColor.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceFileColor.setListener(new MyPreference.MyPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    myPreferenceFileColor.getImgPremium().setVisibility(View.VISIBLE);
                    onGenerateReview("123");
                }
            });

            /*Multiple scan*/
            myPreferenceMultipleScan = (MySwitchPreference) findPreference(getString(R.string.key_multiple_scan));
            myPreferenceMultipleScan.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceMultipleScan.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceMultipleScan.setListener(new MySwitchPreference.MySwitchPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    myPreferenceMultipleScan.getImgPremium().setVisibility(View.VISIBLE);
                }
            });

            /*Skip duplicates*/
            mySwitchPreferenceSkipDuplicates = (MySwitchPreference) findPreference(getString(R.string.key_skip_duplicates));
            mySwitchPreferenceSkipDuplicates.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mySwitchPreferenceSkipDuplicates.setOnPreferenceChangeListener(createChangeListener());
            mySwitchPreferenceSkipDuplicates.setListener(new MySwitchPreference.MySwitchPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    mySwitchPreferenceSkipDuplicates.getImgPremium().setVisibility(View.VISIBLE);
                }
            });

            /*Backup data*/
            mySwitchPreferenceBackupData = (MySwitchPreference) findPreference(getString(R.string.key_backup_data));
            mySwitchPreferenceBackupData.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mySwitchPreferenceBackupData.setOnPreferenceChangeListener(createChangeListener());
            mySwitchPreferenceBackupData.setListener(new MySwitchPreference.MySwitchPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    mySwitchPreferenceBackupData.getImgPremium().setVisibility(View.VISIBLE);
                }
            });

            myPreferenceCategoryFamilyApps = (MyPreferenceCategory) findPreference(getString(R.string.key_family_apps));
            myPreferenceCategoryFamilyApps.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceCategoryFamilyApps.setOnPreferenceChangeListener(createChangeListener());

            /*SuperSafe*/
            myPreferenceSuperSafe = (MyPreference) findPreference(getString(R.string.key_supersafe));
            myPreferenceSuperSafe.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceSuperSafe.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceSuperSafe.setListener(new MyPreference.MyPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    if (myPreferenceSuperSafe.getImgSuperSafe()!=null){
                        myPreferenceSuperSafe.getImgSuperSafe().setImageResource(R.drawable.ic_supersafe_launcher);
                        myPreferenceSuperSafe.getImgSuperSafe().setVisibility(View.VISIBLE);
                    }
                }
            });

            final Author author = Author.getInstance().getAuthorInfo();
            if (author!=null){
                if (author.version!=null){
                    if (author.version.isShowFamilyApps){
                        myPreferenceCategoryFamilyApps.setVisible(true);
                        myPreferenceSuperSafe.setVisible(true);
                    }
                    else {
                        myPreferenceCategoryFamilyApps.setVisible(false);
                        myPreferenceSuperSafe.setVisible(false);
                    }
                }else {
                    myPreferenceCategoryFamilyApps.setVisible(false);
                    myPreferenceSuperSafe.setVisible(false);
                }
            }
            else{
                myPreferenceCategoryFamilyApps.setVisible(false);
                myPreferenceSuperSafe.setVisible(false);
            }
        }

        public void onGenerateReview(String code){
            try {
                if (myPreferenceFileColor==null){
                    if (myPreferenceFileColor.getImageView()==null){
                        return;
                    }
                    return;
                }
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
                hints.put(EncodeHintType.MARGIN, 2);
                Theme theme = Theme.getInstance().getThemeInfo();
                bitmap = barcodeEncoder.encodeBitmap(getContext(),theme.getPrimaryDarkColor(),code, BarcodeFormat.QR_CODE, 100, 100,hints);
                myPreferenceFileColor.getImageView().setImageBitmap(bitmap);
                myPreferenceFileColor.getImageView().setVisibility(View.VISIBLE);
                Utils.Log(TAG,"onGenerateReview");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
        }

        public void onSuperSafe() {
            Uri uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live));
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))));
            }
        }

        public void onRateApp() {
            Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_free_release));
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_free_release))));
            }
        }

        public void onRateProApp() {
            Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_pro_release));
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_pro_release))));
            }
        }
    }
}
