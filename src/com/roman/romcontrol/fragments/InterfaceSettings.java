/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.roman.romcontrol.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.View;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;
import com.roman.romcontrol.util.EosMultiSelectListPreference;

public class InterfaceSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    EosMultiSelectListPreference mStandardSettingsView;
    EosMultiSelectListPreference mEosQuickSettingsView;
    CheckBoxPreference mBatteryText;
    CheckBoxPreference mBatteryIcon;
    CheckBoxPreference mHideStatusBar;
    CheckBoxPreference mLockscreenRotation;
    CheckBoxPreference mSystemUISettings;
    ListPreference mSystemUISettingsLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.eos_interface_settings);

        if (findPreference("eos_interface_normal_settings") != null) {
            mStandardSettingsView = (EosMultiSelectListPreference) findPreference("eos_interface_normal_settings");
            mStandardSettingsView.setOnPreferenceChangeListener(this);
            mStandardSettingsView.setEntries(getResources().getStringArray(
                    R.array.eos_interface_settings_standard_entries));
            mStandardSettingsView.setEntryValues(getResources().getStringArray(
                    R.array.eos_interface_settings_standard_values));
            populateStandardSettingsList();
        }
        
        mEosQuickSettingsView = (EosMultiSelectListPreference) findPreference("eos_interface_eos_quick_enabled");
        if (mEosQuickSettingsView != null) {
            mEosQuickSettingsView.setOnPreferenceChangeListener(this);
            mEosQuickSettingsView.setEntries(getResources().getStringArray(
                    R.array.eos_quick_enabled_names));
            mEosQuickSettingsView.setEntryValues(getResources().getStringArray(
                    R.array.eos_quick_enabled_preferences));
            mEosQuickSettingsView.setReturnFullList(true);
            populateEosSettingsList();
        }

        mSystemUISettings = (CheckBoxPreference) findPreference("eos_interface_settings_eos_enabled");
        if (mSystemUISettings != null){
            mSystemUISettings.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED, 0) == 1);
            mSystemUISettings.setOnPreferenceChangeListener(this);
        }

        mSystemUISettingsLocation = (ListPreference) findPreference("eos_interface_settings_eos_settings_location");
        if (mSystemUISettingsLocation != null) {
            if (Settings.System.getInt(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED, 0) == 0){
                mSystemUISettingsLocation.setValue("disabled");
                mSystemUISettingsLocation.notifyDependencyChange(true);
                mEosQuickSettingsView.setEnabled(false);
            }else if (Settings.System.getInt(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_SETTINGS_PHONE_TOP, 0) == 1) {
                mSystemUISettingsLocation.setValue("top");
                mSystemUISettingsLocation.notifyDependencyChange(false);
                mEosQuickSettingsView.setEnabled(true);
            }else if (Settings.System.getInt(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_SETTINGS_PHONE_BOTTOM, 0) == 1) {
                mSystemUISettingsLocation.setValue("bottom");
                mSystemUISettingsLocation.notifyDependencyChange(false);
                mEosQuickSettingsView.setEnabled(true);
            }
            mSystemUISettingsLocation.setOnPreferenceChangeListener(this);
        }

        mHideStatusBar = (CheckBoxPreference) findPreference("eos_interface_statusbar_visibility");
        if (mHideStatusBar != null) {
            mHideStatusBar
                    .setChecked(Settings.System.getInt(getContentResolver(),
                            Settings.System.EOS_SYSTEMUI_STATUSBAR_VISIBILITY_POWER_OPTION,
                            Settings.System.EOS_SYSTEMUI_STATUSBAR_VISIBILITY_POWER_OPTION_DEF)
                            == 1);
            mHideStatusBar.setOnPreferenceChangeListener(this);
        }
    }

    private void populateStandardSettingsList() {
        HashSet<String> selectedvalues = new HashSet<String>();
        int defaultValues[] = getResources().getIntArray(
                R.array.eos_interface_settings_standard_defaults);
        String preferences[] = getResources().getStringArray(
                R.array.eos_interface_settings_standard_values);

        for (int i = 0; i < preferences.length; i++) {
            if (Settings.System.getInt(getContentResolver(), preferences[i], defaultValues[i]) == 1)
                selectedvalues.add(preferences[i]);

        }
        mStandardSettingsView.setValues(selectedvalues);
    }
    
    private void populateEosSettingsList() {
        LinkedHashSet<String> selectedValues = new LinkedHashSet<String>();
        String enabledControls = Settings.System.getString(getContentResolver(), Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED_CONTROLS);
        if (enabledControls != null){
            String[] controls = enabledControls.split("\\|");
            selectedValues.addAll(Arrays.asList(controls));
        }else {
            selectedValues.addAll(Arrays.asList(Settings.System.EOS_SYSTEMUI_SETTINGS_DEFAULTS));
        }
        mEosQuickSettingsView.setValues(selectedValues);

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!manager.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)){
            mEosQuickSettingsView.removeValueEntry(Settings.System.EOS_SYSTEMUI_SETTINGS_MOBILEDATA);
            mEosQuickSettingsView.removeValueEntry(Settings.System.EOS_SYSTEMUI_SETTINGS_WIFITETHER);
            mEosQuickSettingsView.removeValueEntry(Settings.System.EOS_SYSTEMUI_SETTINGS_USBTETHER);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference.equals(mStandardSettingsView)) {
            Map<String, Boolean> changes = (Map<String, Boolean>) objValue;
            for (Entry<String, Boolean> entry : changes.entrySet()) {
                Settings.System.putInt(getContentResolver(), entry.getKey(), (entry.getValue() ? 1
                        : 0));
            }
            return true;
        } else if (preference.equals(mEosQuickSettingsView)) {
            Map<String, Boolean> values = (Map<String, Boolean>) objValue;
            StringBuilder newPreferenceValue = new StringBuilder();
            for (Entry entry : values.entrySet()) {
                newPreferenceValue.append(entry.getKey());
                newPreferenceValue.append("|");
            }
            Settings.System.putString(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED_CONTROLS,
                    newPreferenceValue.toString());
            return true;
        } else if (preference.equals(mSystemUISettings)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED,
                    ((Boolean) objValue).booleanValue() ? 1 : 0);
        } else if (preference.equals(mSystemUISettingsLocation)) {
            String value = (String) objValue;
            if (value.equals("disabled")) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED, 0);
                preference.notifyDependencyChange(true);
                mEosQuickSettingsView.setEnabled(false);
            } else if (value.equals("top")) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED, 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_PHONE_TOP, 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_PHONE_BOTTOM, 0);
                preference.notifyDependencyChange(false);
                mEosQuickSettingsView.setEnabled(true);
            } else if (value.equals("bottom")) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_ENABLED, 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_PHONE_TOP, 0);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.EOS_SYSTEMUI_SETTINGS_PHONE_BOTTOM, 1);
                preference.notifyDependencyChange(false);
                mEosQuickSettingsView.setEnabled(true);
            }
        } else if (preference.equals(mHideStatusBar)){
            Settings.System.putInt(getContentResolver(),
                    Settings.System.EOS_SYSTEMUI_STATUSBAR_VISIBILITY_POWER_OPTION,
                    ((Boolean) objValue).booleanValue() ? 1 : 0);
        }
        return true;
    }
}
