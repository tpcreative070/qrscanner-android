package tpcreative.co.qrscanner.ui.settings;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.GoalRow;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.mrapp.android.dialog.MaterialDialog;
import de.mrapp.android.preference.ListPreference;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.preference.MyPreference;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG = SettingsFragmentPreference.class.getSimpleName() + "::fragmentTag";
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
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }


    public static class SettingsFragmentPreference extends PreferenceFragmentCompat {

        /**
         * The {@link ListPreference}.
         */

        private MyPreference mVersionApp;

        private MyPreference mPreferencePremiumVersion;

        private MyPreference myPreferenceShare;

        private MyPreference myPreferencePermissions;

        private MyPreference myPreferenceRate;
        private MyPreference myPreferenceRatePro;

        private MyPreference myPreferenceSupport;


        /**
         * Initializes the preference, which allows to change the app's theme.
         */



        public void askPermission(){
            PrefsController.putBoolean(getString(R.string.key_already_load_app),true);
            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext());
            dialogBuilder.setTitle(R.string.app_permission);
            StringBuilder builder = new StringBuilder();
            builder.append("1. WRITE_EXTERNAL_STORAGE: Save history strip to local data");
            builder.append("\n");
            builder.append("2. READ_EXTERNAL_STORAGE: Reading ringtone");
            builder.append("\n");
            builder.append("3. INTERNET,CHANGE_NETWORK_STATE,ACCESS_WIFI_STATE,CHANGE_WIFI_STATE,ACCESS_NETWORK_STATE: Listener disconnect and connect in order to service for premium version");
            builder.append("\n");
            builder.append("4. ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION : Getting longitude and latitude for QRCode type of location");
            builder.append("\n");
            builder.append("5. android.permission.CAMERA: Scanner code");
            builder.append("\n");
            builder.append("6. android.permission.CALL_PHONE: Share QRCode to your phone call");
            dialogBuilder.setMessage(builder.toString());
            dialogBuilder.setPositiveButton(R.string.got_it, null);
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
                    if (preference instanceof MyPreference){
                    }
                    return true;
                }
            };
        }

        public void shareToSocial(String value){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,value);
            startActivity(Intent.createChooser(intent, "Share"));
        }

        private Preference.OnPreferenceClickListener createActionPreferenceClickListener() {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                     if (preference instanceof MyPreference){
                        if (preference.getKey().equals(getString(R.string.key_help))){
                        }
                        else if (preference.getKey().equals(getString(R.string.key_app_permissions))){
                            askPermission();
                        }
                        else if (preference.getKey().equals(getString(R.string.key_share))){
                            if (BuildConfig.BUILD_TYPE.equals(getString(R.string.release)) ||BuildConfig.BUILD_TYPE.equals(getString(R.string.debug))){
                                shareToSocial(getString(R.string.scanner_app_pro));
                            }
                            else{
                                shareToSocial(getString(R.string.scanner_app));
                            }
                        }
                        else if (preference.getKey().equals(getString(R.string.key_support))){
                             try{
                                 Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" +"tpcreative.co@gmail.com"));
                                 intent.putExtra(Intent.EXTRA_SUBJECT, "QRScanner App Support");
                                 intent.putExtra(Intent.EXTRA_TEXT, "");
                                 startActivity(intent);
                             }catch(ActivityNotFoundException e){
                                 //TODO smth
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
            mVersionApp.setSummary(String.format("v%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

            /*Premium*/
            mPreferencePremiumVersion = (MyPreference)findPreference(getString(R.string.key_premium_version));
            //mPreferencePremiumVersion.setOnPreferenceChangeListener(createChangeListener());
            //mPreferencePremiumVersion.setOnPreferenceClickListener(createActionPreferenceClickListener());

            if (BuildConfig.BUILD_TYPE.equals(getString(R.string.release)) ||BuildConfig.BUILD_TYPE.equals(getString(R.string.debug))){
                mPreferencePremiumVersion.setVisible(false);
            }
            else{
                mPreferencePremiumVersion.setVisible(true);
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
            //myPreferenceRate.setOnPreferenceChangeListener(createChangeListener());
           // myPreferenceRate.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Rate Pro*/
            myPreferenceRatePro = (MyPreference) findPreference(getString(R.string.key_rate_pro));
            //myPreferenceRatePro.setOnPreferenceChangeListener(createChangeListener());
           // myPreferenceRatePro.setOnPreferenceClickListener(createActionPreferenceClickListener());

            /*Support*/
            myPreferenceSupport = (MyPreference) findPreference(getString(R.string.key_support));
            myPreferenceSupport.setOnPreferenceClickListener(createActionPreferenceClickListener());
            myPreferenceSupport.setOnPreferenceChangeListener(createChangeListener());


            if (BuildConfig.BUILD_TYPE.equals(getString(R.string.release)) || BuildConfig.BUILD_TYPE.equals(getString(R.string.debug))){
                myPreferenceRatePro.setVisible(true);
                myPreferenceRate.setVisible(false);
            }
            else{
                myPreferenceRatePro.setVisible(false);
                myPreferenceRate.setVisible(true);
            }

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_general);
        }


    }



}
