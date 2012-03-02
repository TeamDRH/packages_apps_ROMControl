
package com.teamdrh.control.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;

import com.teamdrh.control.R;
import com.teamdrh.control.SettingsPreferenceFragment;
import com.teamdrh.control.util.CMDProcessor;
import com.teamdrh.control.util.Helpers;

public class PropModder extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "ROMControl :PropModder";
    private static final String APPEND_CMD = "echo \"%s=%s\" >> /system/build.prop";
    private static final String KILL_PROP_CMD = "busybox sed -i \"/%s/D\" /system/build.prop";
    private static final String REPLACE_CMD = "busybox sed -i \"/%s/ c %<s=%s\" /system/build.prop";
    private static final String REBOOT_PREF = "reboot";
    private static final String FIND_CMD = "grep -q \"%s\" /system/build.prop";
    private static final String REMOUNT_CMD = "busybox mount -o %s,remount -t yaffs2 /dev/block/mtdblock1 /system";
    private static final String PROP_EXISTS_CMD = "grep -q %s /system/build.prop";
    private static final String DISABLE = "disable";
    private static final String SHOWBUILD_PATH = "/system/tmp/showbuild";
    private static final String WIFI_SCAN_PREF = "pref_wifi_scan_interval";
    private static final String WIFI_SCAN_PROP = "wifi.supplicant_scan_interval";
    private static final String WIFI_SCAN_PERSIST_PROP = "persist.wifi_scan_interval";
    private static final String WIFI_SCAN_DEFAULT = System.getProperty(WIFI_SCAN_PROP);
    private static final String LCD_DENSITY_PREF = "pref_lcd_density";
    private static final String LCD_DENSITY_PROP = "ro.sf.lcd_density";
    private static final String LCD_DENSITY_PERSIST_PROP = "persist.lcd_density";
    private static final String LCD_DENSITY_DEFAULT = System.getProperty(LCD_DENSITY_PROP);
    private static final String MAX_EVENTS_PREF = "pref_max_events";
    private static final String MAX_EVENTS_PROP = "windowsmgr.max_events_per_sec";
    private static final String MAX_EVENTS_PERSIST_PROP = "persist.max_events";
    private static final String MAX_EVENTS_DEFAULT = System.getProperty(MAX_EVENTS_PROP);
    private static final String USB_MODE_PREF = "pref_usb_mode";
    private static final String USB_MODE_PROP = "ro.default_usb_mode";
    private static final String USB_MODE_PERSIST_PROP = "persist.usb_mode";
    private static final String USB_MODE_DEFAULT = System.getProperty(USB_MODE_PROP);
    private static final String VM_HEAPSTARTSIZE_PREF = "pref_vm_heapstartsize";
    private static final String VM_HEAPSTARTSIZE_PROP = "dalvik.vm.heapstartsize";
    private static final String VM_HEAPSTARTSIZE_PERSIST_PROP = "persist.vm_heapstartsize";
    private static final String VM_HEAPSTARTSIZE_DEFAULT = System.getProperty(VM_HEAPSTARTSIZE_PROP);
    private static final String VM_HEAPGROWTHLIMIT_PREF = "pref_vm_heapgrowthlimit";
    private static final String VM_HEAPGROWTHLIMIT_PROP = "dalvik.vm.heapgrowthlimit";
    private static final String VM_HEAPGROWTHLIMIT_PERSIST_PROP = "persist.vm_heapgrowthlimit";
    private static final String VM_HEAPGROWTHLIMIT_DEFAULT = System.getProperty(VM_HEAPGROWTHLIMIT_PROP);
    private static final String VM_HEAPSIZE_PREF = "pref_vm_heapsize";
    private static final String VM_HEAPSIZE_PROP = "dalvik.vm.heapsize";
    private static final String VM_HEAPSIZE_PERSIST_PROP = "persist.vm_heapsize";
    private static final String VM_HEAPSIZE_DEFAULT = System.getProperty(VM_HEAPSIZE_PROP);
    private static final String DISABLE_BOOT_ANIM_PREF = "pref_disable_boot_anim";
    private static final String DISABLE_BOOT_ANIM_PROP_1 = "ro.kernel.android.bootanim";
    private static final String DISABLE_BOOT_ANIM_PROP_2 = "debug.sf.nobootanimation";
    private static final String DISABLE_BOOT_ANIM_PERSIST_PROP = "persist.disable_boot_anim";
    private static final String MOD_VERSION_PREF = "pref_mod_version";
    private static final String MOD_VERSION_PROP = "ro.build.display.id";
    private static final String MOD_VERSION_PERSIST_PROP = "persist.build.display.id";
    private static final String MOD_VERSION_DEFAULT = System.getProperty(MOD_VERSION_PROP);
    private static final String MOD_BUTTON_TEXT = "doMod";
    private static final String MOD_VERSION_TEXT = "Mods by PropModder";
    private static final String SLEEP_PREF = "pref_sleep";
    private static final String SLEEP_PROP = "pm.sleep_mode";
    private static final String SLEEP_PERSIST_PROP = "persist.sleep";
    private static final String SLEEP_DEFAULT = System.getProperty(SLEEP_PROP);
    private static final String TCP_STACK_PREF = "pref_tcp_stack";
    private static final String TCP_STACK_PERSIST_PROP = "persist_tcp_stack";
    private static final String TCP_STACK_PROP_0 = "net.tcp.buffersize.default";
    private static final String TCP_STACK_PROP_1 = "net.tcp.buffersize.wifi";
    private static final String TCP_STACK_PROP_2 = "net.tcp.buffersize.umts";
    private static final String TCP_STACK_PROP_3 = "net.tcp.buffersize.gprs";
    private static final String TCP_STACK_PROP_4 = "net.tcp.buffersize.edge";
    private static final String TCP_STACK_BUFFER = "4096,87380,256960,4096,16384,256960";
    private static final String JIT_PREF = "pref_jit";
    private static final String JIT_PERSIST_PROP = "persist_jit";
    private static final String JIT_PROP = "dalvik.vm.execution-mode";
    private static final String CHECK_IN_PREF = "pref_check_in";
    private static final String CHECK_IN_PERSIST_PROP = "persist_check_in";
    private static final String CHECK_IN_PROP = "ro.config.nocheckin";
    private static final String CHECK_IN_PROP_HTC = "ro.config.htc.nocheckin";
    private static final String GPU_PREF = "pref_gpu";
    private static final String GPU_PERSIST_PROP = "persist_gpu";
    private static final String GPU_PROP = "debug.sf.hw";
    private static final String BUILD_FINGERPRINT_PREF = "pref_build_fingerprint";
    private static final String BUILD_FINGERPRINT_PROP = "ro.build.fingerprint";
    private static final String BUILD_FINGERPRINT_PERSIST_PROP = "persist.build_fingerprint";
    private static final String BUILD_FINGERPRINT_DEFAULT = System.getProperty(BUILD_FINGERPRINT_PROP);

    private String placeholder;
    private String tcpstack0;
    private String jitVM;

    private String ModPrefHolder = SystemProperties.get(MOD_VERSION_PERSIST_PROP,
                SystemProperties.get(MOD_VERSION_PROP, MOD_VERSION_DEFAULT));

    //handles for our menu hard key press
    private final int MENU_MARKET = 1;
    private final int MENU_REBOOT = 2;
    private int NOTE_ID;

    private PreferenceScreen mRebootMsg;
    private ListPreference mWifiScanPref;
    private ListPreference mLcdDensityPref;
    private ListPreference mMaxEventsPref;
    private ListPreference mVmHeapStartsizePref;
    private ListPreference mVmHeapGrowthlimitPref;
    private ListPreference mVmHeapsizePref;
    private CheckBoxPreference mDisableBootAnimPref;
    private EditTextPreference mModVersionPref;
    private ListPreference mSleepPref;
    private CheckBoxPreference mTcpStackPref;
    private CheckBoxPreference mJitPref;
    private CheckBoxPreference mCheckInPref;
    private CheckBoxPreference mGpuPref;
    private ListPreference mBuildFingerprintPref;
    private AlertDialog mAlertDialog;
    private NotificationManager mNotificationManager;

    //handler for command processor
    private final CMDProcessor cmd = new CMDProcessor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.propmodder);
        PreferenceScreen prefSet = getPreferenceScreen();

        mRebootMsg = (PreferenceScreen) prefSet.findPreference(REBOOT_PREF);

        mWifiScanPref = (ListPreference) prefSet.findPreference(WIFI_SCAN_PREF);
        mWifiScanPref.setOnPreferenceChangeListener(this);

        mLcdDensityPref = (ListPreference) prefSet.findPreference(LCD_DENSITY_PREF);
        mLcdDensityPref.setOnPreferenceChangeListener(this);

        mMaxEventsPref = (ListPreference) prefSet.findPreference(MAX_EVENTS_PREF);
        mMaxEventsPref.setOnPreferenceChangeListener(this);

        mVmHeapStartsizePref = (ListPreference) prefSet.findPreference(VM_HEAPSTARTSIZE_PREF);
        mVmHeapStartsizePref.setOnPreferenceChangeListener(this);

        mVmHeapGrowthlimitPref = (ListPreference) prefSet.findPreference(VM_HEAPGROWTHLIMIT_PREF);
        mVmHeapGrowthlimitPref.setOnPreferenceChangeListener(this);

        mVmHeapsizePref = (ListPreference) prefSet.findPreference(VM_HEAPSIZE_PREF);
        mVmHeapsizePref.setOnPreferenceChangeListener(this);

        mDisableBootAnimPref = (CheckBoxPreference) prefSet.findPreference(DISABLE_BOOT_ANIM_PREF);

        mSleepPref = (ListPreference) prefSet.findPreference(SLEEP_PREF);
        mSleepPref.setOnPreferenceChangeListener(this);

        mTcpStackPref = (CheckBoxPreference) prefSet.findPreference(TCP_STACK_PREF);

        mJitPref = (CheckBoxPreference) prefSet.findPreference(JIT_PREF);

        mBuildFingerprintPref = (ListPreference) prefSet.findPreference(BUILD_FINGERPRINT_PREF);
        mBuildFingerprintPref.setOnPreferenceChangeListener(this);

        mModVersionPref = (EditTextPreference) prefSet.findPreference(MOD_VERSION_PREF);
        String mod = Helpers.findBuildPropValueOf(MOD_VERSION_PROP);
        if (mModVersionPref != null) {
            EditText modET = mModVersionPref.getEditText();
            ModPrefHolder = mModVersionPref.getEditText().toString();
            if (modET != null){
                InputFilter lengthFilter = new InputFilter.LengthFilter(32);
                modET.setFilters(new InputFilter[]{lengthFilter});
                modET.setSingleLine(true);
            }
            mModVersionPref.setSummary(String.format(getString(R.string.pref_mod_version_alt_summary), mod));
        }
        Log.d(TAG, String.format("ModPrefHolder = '%s' found build number = '%s'", ModPrefHolder, mod));
        mModVersionPref.setOnPreferenceChangeListener(this);

        mCheckInPref = (CheckBoxPreference) prefSet.findPreference(CHECK_IN_PREF);

        mGpuPref = (CheckBoxPreference) prefSet.findPreference(GPU_PREF);

        updateScreen();

        /*
         * we have some requirements so we check
         * and create /system/tmp if needed
         * TODO: .exists() is ok but we should use
         *     : .isDirectory() and .isFile() to be sure
         *     : as .exists() returns positive if a txt file
         *     : exists @ /system/tmp
         */
        File tmpDir = new File("/system/tmp");
        boolean tmpDir_exists = tmpDir.exists();

        if (!tmpDir_exists) {
            try {
                Log.d(TAG, "We need to make /system/tmp dir");
                mount("rw");
                cmd.su.runWaitFor("mkdir /system/tmp");
            } finally {
                mount("ro");
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "com.teamdrh.control.fragments.PropModder has been paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "com.teamdrh.control.fragments.PropModder is being resumed");
    }

    /* handle CheckBoxPreference clicks */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mDisableBootAnimPref) {
            value = mDisableBootAnimPref.isChecked();
            return doMod(null, DISABLE_BOOT_ANIM_PROP_1, String.valueOf(value ? 0 : 1))
                    && doMod(DISABLE_BOOT_ANIM_PERSIST_PROP,
                            DISABLE_BOOT_ANIM_PROP_2, String.valueOf(value ? 1 : 0));
        } else if (preference == mTcpStackPref) {
            Log.d(TAG, "mTcpStackPref.onPreferenceTreeClick()");
            value = mTcpStackPref.isChecked();
            return doMod(null, TCP_STACK_PROP_0, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_1, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_2, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(null, TCP_STACK_PROP_3, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PERSIST_PROP, TCP_STACK_PROP_4, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE));
        } else if (preference == mJitPref) {
            Log.d(TAG, "mJitPref.onPreferenceTreeClick()");
            value = mJitPref.isChecked();
            return doMod(JIT_PERSIST_PROP, JIT_PROP, String.valueOf(value ? "int:fast" : "int:jit"));
        } else if (preference == mCheckInPref) {
            value = mCheckInPref.isChecked();
            return doMod(null, CHECK_IN_PROP_HTC, String.valueOf(value ? 1 : DISABLE))
            && doMod(CHECK_IN_PERSIST_PROP, CHECK_IN_PROP, String.valueOf(value ? 1 : DISABLE));
        } else if (preference == mGpuPref) {
            value = mGpuPref.isChecked();
            return doMod(GPU_PERSIST_PROP, GPU_PROP, String.valueOf(value ? 1 : DISABLE));
        } else if (preference == mRebootMsg) {
            return cmd.su.runWaitFor("reboot").success();
        }

        return false;
    }

    /* handle ListPreferences and EditTextPreferences */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            Log.e(TAG, "New preference selected: " + newValue);
            if (preference == mWifiScanPref) {
                return doMod(WIFI_SCAN_PERSIST_PROP, WIFI_SCAN_PROP,
                        newValue.toString());
            } else if (preference == mLcdDensityPref) {
                return doMod(LCD_DENSITY_PERSIST_PROP, LCD_DENSITY_PROP,
                        newValue.toString());
            } else if (preference == mMaxEventsPref) {
                return doMod(MAX_EVENTS_PERSIST_PROP, MAX_EVENTS_PROP,
                        newValue.toString());
            } else if (preference == mVmHeapStartsizePref) {
                return doMod(VM_HEAPSTARTSIZE_PERSIST_PROP, VM_HEAPSTARTSIZE_PROP,
                        newValue.toString());
            } else if (preference == mVmHeapGrowthlimitPref) {
                return doMod(VM_HEAPGROWTHLIMIT_PERSIST_PROP, VM_HEAPGROWTHLIMIT_PROP,
                        newValue.toString());
            } else if (preference == mVmHeapsizePref) {
                return doMod(VM_HEAPSIZE_PERSIST_PROP, VM_HEAPSIZE_PROP,
                        newValue.toString());
            } else if (preference == mModVersionPref) {
                 return doMod(MOD_VERSION_PERSIST_PROP, MOD_VERSION_PROP,
                        newValue.toString());
            } else if (preference == mSleepPref) {
                 return doMod(SLEEP_PERSIST_PROP, SLEEP_PROP,
                        newValue.toString());
            } else if (preference == mBuildFingerprintPref) {
                return doMod(BUILD_FINGERPRINT_PERSIST_PROP, BUILD_FINGERPRINT_PROP,
                        newValue.toString());
            }
        }

        return false;
    }

    /* method to handle mods */
    private boolean doMod(String persist, String key, String value) {

        if (persist != null) {
            SystemProperties.set(persist, value);
        }
        Log.d(TAG, String.format("Calling script with args '%s' and '%s'", key, value));
        backupBuildProp();
        if (!mount("rw")) {
            throw new RuntimeException("Could not remount /system rw");
        }
        boolean success = false;
        try {
            if (!propExists(key) && value.equals(DISABLE)) {
                Log.d(TAG, String.format("We want {%s} DISABLED however it doesn't exist so we do nothing and move on", key));
            } else if (propExists(key)) {
                if (value.equals(DISABLE)) {
                    Log.d(TAG, String.format("value == %s", DISABLE));
                    success = cmd.su.runWaitFor(String.format(KILL_PROP_CMD, key)).success();
                } else {
                    Log.d(TAG, String.format("value != %s", DISABLE));
                    success = cmd.su.runWaitFor(String.format(REPLACE_CMD, key, value)).success();
                }

            } else {
                Log.d(TAG, "append command starting");
                success = cmd.su.runWaitFor(String.format(APPEND_CMD, key, value)).success();
            }
            if (!success) {
                restoreBuildProp();
            } else {
                updateScreen();
            }
        } finally {
            mount("ro");
        }

        mRebootMsg.setTitle("Reboot required");
        mRebootMsg.setSummary("values will take effect on next boot");
        mRebootMsg.setEnabled(true);
        return success;
    }

    public boolean mount(String read_value) {
        Log.d(TAG, "Remounting /system " + read_value);
        return cmd.su.runWaitFor(String.format(REMOUNT_CMD, read_value)).success();
    }

    public boolean propExists(String prop) {
        Log.d(TAG, "Checking if prop " + prop + " exists in /system/build.prop");
        return cmd.su.runWaitFor(String.format(PROP_EXISTS_CMD, prop)).success();
    }

    public void updateShowBuild() {
        Log.d(TAG, "Setting up /system/tmp/showbuild");
        try {
            mount("rw");
            cmd.su.runWaitFor("cp -f /system/build.prop " + SHOWBUILD_PATH).success();
            cmd.su.runWaitFor("chmod 777 " + SHOWBUILD_PATH).success();
        } finally {
            mount("ro");
        }
    }

    public boolean backupBuildProp() {
        Log.d(TAG, "Backing up build.prop to /system/tmp/pm_build.prop");
        return cmd.su.runWaitFor("cp /system/build.prop /system/tmp/pm_build.prop").success();
    }
    
    public boolean restoreBuildProp() {
        Log.d(TAG, "Restoring build.prop from /system/tmp/pm_build.prop");
        return cmd.su.runWaitFor("cp /system/tmp/pm_build.prop /system/build.prop").success();
    }

    public void updateScreen() {
        //update all the summaries
        String wifi = Helpers.findBuildPropValueOf(WIFI_SCAN_PROP);
        if (!wifi.equals(DISABLE)) {
            mWifiScanPref.setValue(wifi);
            mWifiScanPref.setSummary(String.format(getString(R.string.pref_wifi_scan_alt_summary), wifi));
        } else {
            mWifiScanPref.setValue(WIFI_SCAN_DEFAULT);
        }
        String lcd = Helpers.findBuildPropValueOf(LCD_DENSITY_PROP);
        if (!lcd.equals(DISABLE)) {
            mLcdDensityPref.setValue(lcd);
            mLcdDensityPref.setSummary(String.format(getString(R.string.pref_lcd_density_alt_summary), lcd));
        } else {
            mLcdDensityPref.setValue(LCD_DENSITY_DEFAULT);
        }
        String maxE = Helpers.findBuildPropValueOf(MAX_EVENTS_PROP);
        if (!maxE.equals(DISABLE)) {
            mMaxEventsPref.setValue(maxE);
            mMaxEventsPref.setSummary(String.format(getString(R.string.pref_max_events_alt_summary), maxE));
        } else {
            mMaxEventsPref.setValue(MAX_EVENTS_DEFAULT);
        }
        String vmss = Helpers.findBuildPropValueOf(VM_HEAPSTARTSIZE_PROP);
        if (!vmss.equals(DISABLE)) {
            mVmHeapStartsizePref.setValue(vmss);
            mVmHeapStartsizePref.setSummary(String.format(getString(R.string.pref_vm_heapstartsize_alt_summary), vmss));
        } else {
            mVmHeapStartsizePref.setValue(VM_HEAPSTARTSIZE_DEFAULT);
        }
        String vmgl = Helpers.findBuildPropValueOf(VM_HEAPGROWTHLIMIT_PROP);
        if (!vmgl.equals(DISABLE)) {
            mVmHeapGrowthlimitPref.setValue(vmgl);
            mVmHeapGrowthlimitPref.setSummary(String.format(getString(R.string.pref_vm_heapgrowthlimit_alt_summary), vmgl));
        } else {
            mVmHeapsizePref.setValue(VM_HEAPSIZE_DEFAULT);
        }
        String vm = Helpers.findBuildPropValueOf(VM_HEAPSIZE_PROP);
        if (!vm.equals(DISABLE)) {
            mVmHeapsizePref.setValue(vm);
            mVmHeapsizePref.setSummary(String.format(getString(R.string.pref_vm_heapsize_alt_summary), vm));
        } else {
            mVmHeapsizePref.setValue(VM_HEAPSIZE_DEFAULT);
        }
        String ba1 = Helpers.findBuildPropValueOf(DISABLE_BOOT_ANIM_PROP_1);
        String ba2 = Helpers.findBuildPropValueOf(DISABLE_BOOT_ANIM_PROP_2);
        if (ba1.equals("0") && ba2.equals("1")) {
            Log.d(TAG, "bootanimation is disabled");
            mDisableBootAnimPref.setChecked(false);
        } else {
            Log.d(TAG, "bootanimation is enabled");
            mDisableBootAnimPref.setChecked(true);
        }
        String sleep = Helpers.findBuildPropValueOf(SLEEP_PROP);
        if (!sleep.equals(DISABLE)) {
            mSleepPref.setValue(sleep);
            mSleepPref.setSummary(String.format(getString(R.string.pref_sleep_alt_summary), sleep));
        } else {
            mSleepPref.setValue(SLEEP_DEFAULT);
        }
        String tcp = Helpers.findBuildPropValueOf(TCP_STACK_PROP_0);
        if (tcp.equals(TCP_STACK_BUFFER)) {
            mTcpStackPref.setChecked(true);
        } else {
            mTcpStackPref.setChecked(false);
        }
        String jit = Helpers.findBuildPropValueOf(JIT_PROP);
        if (jit.equals("int:jit")) {
            mJitPref.setChecked(true);
        } else {
            mJitPref.setChecked(false);
        }
        String mod = Helpers.findBuildPropValueOf(MOD_VERSION_PROP);
        mModVersionPref.setSummary(String.format(getString(R.string.pref_mod_version_alt_summary), mod));
        String chk = Helpers.findBuildPropValueOf(CHECK_IN_PROP);
        if (!chk.equals(DISABLE)) {
            mCheckInPref.setChecked(true);
        } else {
            mCheckInPref.setChecked(false);
        }
        String gpu = Helpers.findBuildPropValueOf(GPU_PROP);
        if (!gpu.equals(DISABLE)) {
            mGpuPref.setChecked(true);
        } else {
            mGpuPref.setChecked(false);
        }
        String fgp = Helpers.findBuildPropValueOf(BUILD_FINGERPRINT_PROP);
        if (!fgp.equals(DISABLE)) {
            mBuildFingerprintPref.setValue(fgp);
            mBuildFingerprintPref.setSummary(String.format(getString(R.string.pref_build_fingerprint_alt_summary), fgp));
        } else {
            mBuildFingerprintPref.setValue(BUILD_FINGERPRINT_DEFAULT);
        }
    }
}
