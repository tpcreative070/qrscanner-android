package tpcreative.co.qrscanner.ui.settings;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.EnumMap;
import java.util.Map;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonSettings;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.preference.MyPreference;
import tpcreative.co.qrscanner.common.preference.MyPreferenceCategory;
import tpcreative.co.qrscanner.common.preference.MySwitchPreference;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.Theme;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsFragmentPreference.class.getSimpleName() + "::fragmentTag";
    @BindView(R.id.llAction)
    LinearLayout llAction;
    private Unbinder unbinder;


    public static SettingsFragment newInstance(int index) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, view);
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            fragment = Fragment.instantiate(getContext(), SettingsFragmentPreference.class.getName());
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            boolean isTip = PrefsController.getBoolean(getString(R.string.key_is_first_help),false);
            if (!isTip){
                llAction.setVisibility(View.VISIBLE);
                onSuggestionTips();
            }
            Log.d(TAG, "isVisible");
        } else {
            Log.d(TAG, "isInVisible");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


    public static class SettingsFragmentPreference extends PreferenceFragmentCompat implements SingletonSettings.SingletonSettingsListener{


        private MyPreference mVersionApp;

        private MyPreference mPreferencePremiumVersion;

        private MyPreference myPreferenceShare;

        private MyPreference myPreferencePermissions;

        private MyPreference myPreferenceRate;

        private MyPreference myPreferenceSupport;

        private MyPreference myPreferenceHelp;

        private MySwitchPreference mySwitchPreferenceVibrate;
        private MySwitchPreference mySwitchPreferenceAskUpdate;

        private MyPreference myPreferenceFileSize;

        private MyPreference myPreferenceTheme;

        private MyPreference myPreferenceFileColor;

        private MyPreference myPreferenceSuperSafe;

        private MyPreferenceCategory myPreferenceCategoryFamilyApps;


        private Bitmap bitmap;


        @Override
        public void onResume() {
            super.onResume();
            SingletonSettings.getInstance().setListener(this);
        }

        @Override
        public void onUpdated() {
            onGenerateReview("123");
        }

        @Override
        public void onUpdatedSharePreferences(boolean value) {
           if (mySwitchPreferenceAskUpdate!=null){
               PrefsController.putBoolean(getString(R.string.key_auto_ask_update),value);
               mySwitchPreferenceAskUpdate.setChecked(value);
           }
        }

        /**
         * Initializes the preference, which allows to change the app's theme.
         */

        public void askPermission() {
            PrefsController.putBoolean(getString(R.string.key_already_load_app), true);
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext(),R.style.DarkDialogTheme);
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
                        Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.brandon_bld);
                        title.setTypeface(typeface,Typeface.BOLD);
                        title.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.colorBlueLight));
                        positive.setTypeface(typeface,Typeface.BOLD);
                        positive.setTextSize(14);
                    }
                }
            });
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
                    if (preference instanceof MyPreference) {
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
                            if (BuildConfig.BUILD_TYPE.equals(getString(R.string.release)) || BuildConfig.BUILD_TYPE.equals(getString(R.string.debug))) {
                                shareToSocial(getString(R.string.scanner_app_pro));
                            } else {
                                shareToSocial(getString(R.string.scanner_app));
                            }
                        } else if (preference.getKey().equals(getString(R.string.key_support))) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "tpcreative.co@gmail.com"));
                                intent.putExtra(Intent.EXTRA_SUBJECT, "QRScanner App Support");
                                intent.putExtra(Intent.EXTRA_TEXT, "");
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                //TODO smth
                            }
                        } else if (preference.getKey().equals(getString(R.string.key_help))) {
                            Log.d(TAG, "action here");
                            Navigator.onMoveToHelp(getContext());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_color_code))){
                            Navigator.onMoveToChangeFileColor(getActivity());
                        }
                        else if (preference.getKey().equals(getString(R.string.key_rate))){
                            if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_live))){
                                onRateApp();
                            }
                            else if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_live_pro))){
                                Utils.Log(TAG,"pro here");
                                onRateProApp();
                            }
                            Utils.Log(TAG,"pro here???");
                        }
                        else if (preference.getKey().equals(getString(R.string.key_supersafe))){
                            onSuperSafe();
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

            /*Premium*/
            mPreferencePremiumVersion = (MyPreference) findPreference(getString(R.string.key_premium_version));
            //mPreferencePremiumVersion.setOnPreferenceChangeListener(createChangeListener());
            //mPreferencePremiumVersion.setOnPreferenceClickListener(createActionPreferenceClickListener());

            if (BuildConfig.BUILD_TYPE.equals(getString(R.string.release)) || BuildConfig.BUILD_TYPE.equals(getString(R.string.debug))) {
                mPreferencePremiumVersion.setVisible(false);
            } else {
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

            /*Theme*/
            myPreferenceTheme = (MyPreference) findPreference(getString(R.string.key_theme));
            myPreferenceTheme.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceTheme.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceTheme.setVisible(false);

            /*File size*/
            myPreferenceFileSize = (MyPreference) findPreference(getString(R.string.key_size_code));
            myPreferenceFileSize.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceFileSize.setOnPreferenceChangeListener(createChangeListener());
            myPreferenceFileSize.setVisible(false);

            /*File color*/
            myPreferenceFileColor = (MyPreference) findPreference(getString(R.string.key_color_code));
            myPreferenceFileColor.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceFileColor.setOnPreferenceChangeListener(createChangeListener());


            myPreferenceFileColor.setListener(new MyPreference.MyPreferenceListener() {
                @Override
                public void onUpdatePreference() {
                    if (myPreferenceFileColor.getImageView() != null) {
                       onGenerateReview("123");
                    } else {
                        Utils.Log(TAG, "Log album cover is null.........");
                    }
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

            /*Vibrate*/
            mySwitchPreferenceAskUpdate = (MySwitchPreference) findPreference(getString(R.string.key_auto_ask_update));
            mySwitchPreferenceAskUpdate.setOnPreferenceClickListener(createActionPreferenceClickListener());
            mySwitchPreferenceAskUpdate.setOnPreferenceChangeListener(createChangeListener());
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
            Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_live));
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
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_live))));
            }
        }


        public void onRateProApp() {
            Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_live_pro));
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
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_live_pro))));
            }
        }

    }

    public void onSuggestionTips(){
        Typeface typeface = ResourcesCompat.getFont(QRScannerApplication.getInstance(), R.font.brandon_reg);
        TapTargetView.showFor(getActivity(),                 // `this` is an Activity
                TapTarget.forView(llAction, getString(R.string.tap_here_to_discover_how_to_scan), getString(R.string.tap_here_to_discover_how_to_scan_description))
                        .titleTextSize(25)
                        .titleTextColor(R.color.white)
                        .descriptionTextColor(R.color.black)
                        .titleTypeface(typeface)
                        .descriptionTypeface(typeface)
                        .descriptionTextSize(17)
                        .outerCircleColor(R.color.colorButton)
                        .transparentTarget(true)
                        .targetCircleColor(R.color.white)
                        .cancelable(true)
                        .dimColor(R.color.white),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        Navigator.onMoveToHelp(getContext());
                        PrefsController.putBoolean(getString(R.string.key_is_first_help),true);
                        llAction.setVisibility(View.INVISIBLE);
                        Utils.Log(TAG,"onTargetClick");
                        view.dismiss(true);
                    }

                    @Override
                    public void onOuterCircleClick(TapTargetView view) {
                        super.onOuterCircleClick(view);
                        PrefsController.putBoolean(getString(R.string.key_is_first_help),true);
                        llAction.setVisibility(View.INVISIBLE);
                        Utils.Log(TAG,"onOuterCircleClick");
                        view.dismiss(true);
                    }

                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        super.onTargetDismissed(view, userInitiated);
                        Utils.Log(TAG,"onTargetDismissed");
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        super.onTargetCancel(view);
                        PrefsController.putBoolean(getString(R.string.key_is_first_help),true);
                        llAction.setVisibility(View.INVISIBLE);
                        Utils.Log(TAG,"onTargetCancel");
                    }
                });
    }


}
