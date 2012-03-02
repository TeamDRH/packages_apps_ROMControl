
package com.teamdrh.control.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.teamdrh.control.R;

public class PowerMenu extends PreferenceFragment {

    private static final String PREF_POWER_MENU = "show_power_menu";
    private static final String PREF_REBOOT_MENU = "show_reboot_menu";
    private static final String PREF_PROFILES_MENU = "show_profiles_menu";
    private static final String PREF_SCREENSHOT = "show_screenshot";
    private static final String PREF_AIRPLANE_MODE = "show_airplane_mode";
    
    private static final String PREF_TORCH_TOGGLE = "show_torch_toggle";
    private static final String PREF_NAVBAR_HIDE = "show_navbar_hide";

    CheckBoxPreference mShowPowerMenu;
    CheckBoxPreference mShowRebootMenu;
    CheckBoxPreference mShowProfilesMenu;
    CheckBoxPreference mShowAirplaneMode;
    CheckBoxPreference mShowScreenShot;
    
    CheckBoxPreference mShowTorchToggle;
    CheckBoxPreference mShowNavBarHide;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_powermenu);

        mShowPowerMenu = (CheckBoxPreference) findPreference(PREF_POWER_MENU);
        mShowPowerMenu.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.POWER_DIALOG_SHOW_POWER_MENU, 1) == 1);

        mShowRebootMenu = (CheckBoxPreference) findPreference(PREF_REBOOT_MENU);
        mShowRebootMenu.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.POWER_DIALOG_SHOW_REBOOT_MENU, 1) == 1);
        
        mShowProfilesMenu = (CheckBoxPreference) findPreference(PREF_PROFILES_MENU);
        mShowProfilesMenu.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.POWER_DIALOG_SHOW_PROFILES_MENU, 1) == 1);

        mShowScreenShot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
        mShowScreenShot.setChecked(Settings.System.getInt(getActivity().getContentResolver(), 
                Settings.System.POWER_DIALOG_SHOW_SCREENSHOT, 1) == 1);

        mShowAirplaneMode = (CheckBoxPreference) findPreference(PREF_AIRPLANE_MODE);
        mShowAirplaneMode.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.POWER_DIALOG_SHOW_AIRPLANE_MODE, 1) == 1);

        mShowTorchToggle = (CheckBoxPreference) findPreference(PREF_TORCH_TOGGLE);
        mShowTorchToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POWER_DIALOG_SHOW_TORCH_TOGGLE, 0) == 1);

        mShowNavBarHide = (CheckBoxPreference) findPreference(PREF_NAVBAR_HIDE);
        mShowNavBarHide.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE, 0) == 1);

        if (getResources().getBoolean(R.bool.use_gtab_only)) {
            getPreferenceScreen().removePreference(mShowTorchToggle);
            getPreferenceScreen().removePreference(mShowNavBarHide);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mShowPowerMenu) {
            Log.d( "PowerMenu", "SHOW_POWER_MENU: " + ((CheckBoxPreference) preference).isChecked() );
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_POWER_MENU,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowRebootMenu) {
            Log.d( "PowerMenu", "SHOW_REBOOT_MENU: " + ((CheckBoxPreference) preference).isChecked() );
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_REBOOT_MENU,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowProfilesMenu) {
            Log.d( "PowerMenu", "SHOW_PROFILES_MENU: " + ((CheckBoxPreference) preference).isChecked() );
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_PROFILES_MENU,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowScreenShot) {
            Log.d( "PowerMenu", "SHOW_SCREENSHOT: " + ((CheckBoxPreference) preference).isChecked() );
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowAirplaneMode) {
            Log.d( "PowerMenu", "SHOW_AIRPLANE_MODE: " + ((CheckBoxPreference) preference).isChecked() );
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_AIRPLANE_MODE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowTorchToggle) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_TORCH_TOGGLE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowNavBarHide) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
