
package com.teamdrh.control.fragments;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.teamdrh.control.R;
import com.teamdrh.control.SettingsPreferenceFragment;
import com.teamdrh.control.tools.VoltageControl;
import com.teamdrh.control.util.CMDProcessor;
import com.teamdrh.control.util.Helpers;

public class Performance extends SettingsPreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    public static final String TAG = "Performance";
    public static final String KEY_MAX_CPU = "max_cpu";
    public static final String KEY_MIN_CPU = "min_cpu";
    public static final String KEY_GOV = "gov";
    public static final String KEY_SCHED = "sched";
    public static final String KEY_CPU_BOOT = "cpu_boot";
    public static final String KEY_MINFREE = "free_memory";

    private static final String STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    private static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static final String GETALL_GOV = "sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    private static final String CUR_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    private static final String GETALL_SCHED = "/sys/block/mmcblk0/queue/scheduler";
    private static final String CUR_SCHED = "/sys/block/mmcblk0/queue/scheduler";
    
    private static final String PURGEABLE_ASSETS_PREF = "pref_purgeable_assets";
    private static final String PURGEABLE_ASSETS_PERSIST_PROP = "persist.sys.purgeable_assets";
    private static final String PURGEABLE_ASSETS_DEFAULT = "0";

    private static final String USE_DITHERING_PREF = "pref_use_dithering";
    private static final String USE_DITHERING_PERSIST_PROP = "persist.sys.use_dithering";
    private static final String USE_DITHERING_DEFAULT = "1";

    private static final String USE_16BPP_ALPHA_PREF = "pref_use_16bpp_alpha";
    private static final String USE_16BPP_ALPHA_PROP = "persist.sys.use_16bpp_alpha";
   
    public static final String MINFREE = "/sys/module/lowmemorykiller/parameters/minfree";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";

    private String[] ALL_GOV;
    private String[] ALL_SCHED;
    private int[] SPEED_STEPS;
    private ListPreference mMinCpu;
    private ListPreference mMaxCpu;
    private ListPreference mSetGov;
    private ListPreference mSetSched;

    private CheckBoxPreference mPurgeableAssetsPref;
    private CheckBoxPreference mUseDitheringPref;
    private CheckBoxPreference mUse16bppAlphaPref;

    private ListPreference mFreeMem;
    private ListPreference mScrollingCachePref;
    private SharedPreferences preferences;
    private boolean doneLoading = false;
    
    private AlertDialog alertDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        super.onCreate(savedInstanceState);
        preferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.performance);

        final int frequencies[] = getFrequencies();
        final String freqList[] = getMHz(frequencies);
        final String freqValues[] = getValues(frequencies);
        final String maxFreq = (Helpers.getFile(MAX_FREQ).trim());
        final String minFreq = (Helpers.getFile(MIN_FREQ).trim());
        final String maxInMhz = (Integer.toString((Integer.parseInt(maxFreq) / 1000)) + " MHz");
        final String minInMhz = (Integer.toString((Integer.parseInt(minFreq) / 1000)) + " MHz");
        final String govs[] = getAllGovs();
        final String scheds[] = getAllScheds();
        final String currentGov = (Helpers.getFile(CUR_GOV).trim());
        final String currentSched = getCurrentSched();

        mMaxCpu = (ListPreference) findPreference(KEY_MAX_CPU);
        mMaxCpu.setEntries(freqList);
        mMaxCpu.setEntryValues(freqValues);
        mMaxCpu.setValue(maxFreq);
        mMaxCpu.setSummary(getString(R.string.ps_set_max, maxInMhz));

        mMinCpu = (ListPreference) findPreference(KEY_MIN_CPU);
        mMinCpu.setEntries(freqList);
        mMinCpu.setEntryValues(freqValues);
        mMinCpu.setValue(minFreq);
        mMinCpu.setSummary(getString(R.string.ps_set_min, minInMhz));

        mSetGov = (ListPreference) findPreference(KEY_GOV);
        mSetGov.setEntries(govs);
        mSetGov.setEntryValues(govs);
        mSetGov.setValue(currentGov);
        mSetGov.setSummary(getString(R.string.ps_set_gov, currentGov));

        mSetSched = (ListPreference) findPreference(KEY_SCHED);
        mSetSched.setEntries(scheds);
        mSetSched.setEntryValues(scheds);
        mSetSched.setValue(currentSched);
        mSetSched.setSummary(getString(R.string.ps_set_sched, currentSched));

        mPurgeableAssetsPref = (CheckBoxPreference) findPreference(PURGEABLE_ASSETS_PREF);
        mUseDitheringPref = (CheckBoxPreference) findPreference(USE_DITHERING_PREF);
        mUse16bppAlphaPref = (CheckBoxPreference) findPreference(USE_16BPP_ALPHA_PREF);

        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);

        final int minFree = getMinFreeValue();
        final String values[] = getResources().getStringArray(R.array.minfree_values);
        String closestValue = preferences.getString(KEY_MINFREE, values[0]);

        if (minFree < 62)
            closestValue = values[0];
        else if (minFree < 87)
            closestValue = values[1];
        else if (minFree < 112)
            closestValue = values[2];
        else
            closestValue = values[3];

        mFreeMem = (ListPreference) findPreference(KEY_MINFREE);
        mFreeMem.setValue(closestValue);
        mFreeMem.setSummary(getString(R.string.ps_free_memory, minFree + "mb"));

        PreferenceScreen ps = (PreferenceScreen) findPreference("volt_control");
        if (!new File(VoltageControl.MV_TABLE0).exists()) {
            ((PreferenceCategory) getPreferenceScreen().findPreference("cpu"))
                    .removePreference(ps);
        }

        String purgeableAssets = SystemProperties.get(PURGEABLE_ASSETS_PERSIST_PROP,
                PURGEABLE_ASSETS_DEFAULT);
        mPurgeableAssetsPref.setChecked("1".equals(purgeableAssets));

        String useDithering = SystemProperties.get(USE_DITHERING_PERSIST_PROP,
                USE_DITHERING_DEFAULT);
        mUseDitheringPref.setChecked("1".equals(useDithering));

        String use16bppAlpha = SystemProperties.get(USE_16BPP_ALPHA_PROP, "0");
        mUse16bppAlphaPref.setChecked("1".equals(use16bppAlpha));

        /* Display the warning dialog */
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(R.string.performance_settings_warning_title);
        alertDialog.setMessage(getResources().getString(R.string.performance_settings_warning));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(com.android.internal.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

        alertDialog.show();

        doneLoading = true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mPurgeableAssetsPref) {
            SystemProperties.set(PURGEABLE_ASSETS_PERSIST_PROP,
                    mPurgeableAssetsPref.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mUseDitheringPref) {
            SystemProperties.set(USE_DITHERING_PERSIST_PROP,
                    mUseDitheringPref.isChecked() ? "1" : "0");
        } else if (preference == mUse16bppAlphaPref) {
            SystemProperties.set(USE_16BPP_ALPHA_PROP,
                    mUse16bppAlphaPref.isChecked() ? "1" : "0");
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return false;
    }

    @Override
    public void onSharedPreferenceChanged(
            final SharedPreferences sharedPreferences, String key) {
        if (doneLoading) {
            if (key.equals(KEY_MAX_CPU)) {
                final String value = preferences.getString(key, null);
                final String maxInMhz = (Integer.toString((Integer.parseInt(value) / 1000)) + " MHz");
                if (!sendCpu(key, value, MAX_FREQ))
                    Helpers.sendMsg(getActivity(),
                            getString(R.string.toast_min_max_error01));
                else
                    mMaxCpu.setSummary(getString(R.string.ps_set_max, maxInMhz));
            } else if (key.equals(KEY_MIN_CPU)) {
                final String value = preferences.getString(key, null);
                final String minInMhz = (Integer.toString((Integer.parseInt(value) / 1000)) + " MHz");
                if (!sendCpu(key, value, MIN_FREQ))
                    Helpers.sendMsg(getActivity(),
                            getString(R.string.toast_min_max_error02));
                else
                    mMinCpu.setSummary(getString(R.string.ps_set_min, minInMhz));
            } else if (key.equals(KEY_GOV)) {
                final String value = preferences.getString(key, null);
                if ((new CMDProcessor().su
                        .runWaitFor("busybox echo " + value + " > " + CUR_GOV)).success())
                    mSetGov.setSummary(getString(R.string.ps_set_gov, value));
            } else if (key.equals(KEY_SCHED)) {
                final String value = preferences.getString(key, null);
                if ((new CMDProcessor().su
                        .runWaitFor("busybox echo " + value + " > " + CUR_SCHED)).success())
                    mSetSched.setSummary(getString(R.string.ps_set_sched, value));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mmcblk1"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock0"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock1"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock2"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock3"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock4"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock5"));
                new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + CUR_SCHED.replace("mmcblk0", "mtdblock6"));
            } else if (key.equals(KEY_MINFREE)) {
                String values = preferences.getString(key, null);
                if (!values.equals(null))
                    new CMDProcessor().su
                            .runWaitFor("busybox echo " + values + " > " + MINFREE);
                mFreeMem.setSummary(getString(R.string.ps_free_memory, getMinFreeValue() + "mb"));
            }
        }

    }

    String[] getMHz(int freqs[]) {
        int freqInMHz[] = new int[freqs.length];
        for (int i = 0; i < freqs.length; i++)
            freqInMHz[i] = freqs[i] / 1000;
        String steps[] = new String[freqs.length];
        for (int i = 0; i < freqs.length; i++)
            steps[i] = Integer.toString(freqInMHz[i]) + " MHz";
        return steps;
    }

    String[] getValues(int freqs[]) {
        final String steps[] = new String[freqs.length];
        for (int i = 0; i < freqs.length; i++)
            steps[i] = Integer.toString(freqs[i]);
        return steps;
    }

    int[] getFrequencies() {
        String freqs = Helpers.getFile(STEPS);
        if (freqs != null && freqs != "") {
            String[] freqList = freqs.trim().split("[ \n]+");
            SPEED_STEPS = new int[freqList.length];
            for (int i = 0; i < freqList.length; i++) {
                SPEED_STEPS[i] = (Integer.parseInt(freqList[i]));
            }
        } else {
            CMDProcessor cmd = new CMDProcessor();
            CMDProcessor.CommandResult r = cmd.su
                    .runWaitFor("busybox cut -d ' ' -f1 /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
            freqs = r.stdout;
            if (freqs != null && freqs != "") {
                String[] freqList = freqs.trim().split("[ \n]+");
                SPEED_STEPS = new int[freqList.length];
                for (int i = 0; i < freqList.length; i++) {
                    SPEED_STEPS[i] = (Integer.parseInt(freqList[i]));
                }
            } else {
                SPEED_STEPS = new int[] {
                        1000000, 800000, 600000, 300000
                };
                Log.d(TAG, "Failed getting steps");
            }
        }
        return SPEED_STEPS;
    }

    public String[] getAllGovs() {
        String govs = Helpers.getFile(GETALL_GOV);
        if (govs != null && govs != "") {
            String[] govList = govs.trim().split(" ");
            ALL_GOV = new String[govList.length];
            for (int i = 0; i < govList.length; i++) {
                ALL_GOV[i] = govList[i];
            }
        } else {
            ALL_GOV = new String[] {
                    "ondemand", "userspace", "performance"
            };
        }
        return ALL_GOV;
    }

    public String[] getAllScheds() {
        String scheds = Helpers.getFile(GETALL_SCHED);
        if (scheds != null && scheds != "") {
            String[] schedList = scheds.trim().split(" ");
            ALL_SCHED = new String[schedList.length];
            for (int i = 0; i < schedList.length; i++) {
                if (schedList[i].indexOf("[") == -1) {
                    ALL_SCHED[i] = schedList[i];
                } else {
                    ALL_SCHED[i] = schedList[i].substring(schedList[i].indexOf("[")+1, schedList[i].indexOf("]"));
                }
            }
        } else {
            ALL_SCHED = new String[] {
                    "noop", "deadline", "cfq"
            };
        }

        return ALL_SCHED;
    }

    public String getCurrentSched() {
        String scheds = Helpers.getFile(GETALL_SCHED);

        String currSched = scheds.substring(scheds.indexOf("[") + 1, scheds.indexOf("]"));

        return currSched;
    }

    private int getMinFreeValue() {
        int emptyApp = 0;
        String MINFREE_LINE = Helpers.getFile(MINFREE);
        String EMPTY_APP = MINFREE_LINE.substring(MINFREE_LINE.lastIndexOf(",") + 1);

        if (!EMPTY_APP.equals(null) || !EMPTY_APP.equals("")) {
            try {
                int mb = Integer.parseInt(EMPTY_APP.trim()) * 4 / 1024;
                emptyApp = (int) Math.ceil(mb);
            } catch (NumberFormatException nfe) {
                Log.i(TAG, "error processing " + EMPTY_APP);
            }
        }
        return emptyApp;
    }

    private boolean sendCpu(final String key, final String value, final String fname) {
        final int maxCpu = Integer.parseInt((Helpers.getFile(MAX_FREQ).trim()));
        final int minCpu = Integer.parseInt((Helpers.getFile(MIN_FREQ).trim()));
        final int newCpu = Integer.parseInt(value);
        final CMDProcessor cmd = new CMDProcessor();

        Boolean isOk = true;
        String goodCpu = value;

        if (key.equals(KEY_MAX_CPU)) {
            if (newCpu < minCpu) {
                isOk = false;
                goodCpu = Integer.toString(maxCpu);
            }
        } else if (key.equals(KEY_MIN_CPU)) {
            if (newCpu > maxCpu) {
                isOk = false;
                goodCpu = Integer.toString(minCpu);
            }
        }

        if (isOk) {
            cmd.su.runWaitFor("busybox echo " + value + " > " + fname);
            if (new File("/sys/devices/system/cpu/cpu1").isDirectory()) {
                String cpu1 = fname.replace("cpu0", "cpu1");
                cmd.su.runWaitFor("busybox echo " + value + " > " + cpu1);
            }
        } else {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, goodCpu);
            editor.commit();

            if (key.equals(KEY_MAX_CPU))
                mMaxCpu.setValue(goodCpu);
            else if (key.equals(KEY_MIN_CPU))
                mMinCpu.setValue(goodCpu);
        }

        return isOk;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String) newValue);
                return true;
            }
        }

        return false;
    }

}
