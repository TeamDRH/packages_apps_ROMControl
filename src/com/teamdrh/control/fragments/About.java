
package com.teamdrh.control.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.teamdrh.control.R;
import com.teamdrh.control.SettingsPreferenceFragment;

public class About extends SettingsPreferenceFragment {

    public static final String TAG = "About";

    Preference mSiteUrl;
    Preference mSourceUrl;
    Preference mIrcUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs_about);
        mSiteUrl = findPreference("teamdrh_website");
        mSourceUrl = findPreference("teamdrh_source");
        mIrcUrl = findPreference("team_irc");

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://teamdrh.com/");
        } else if (preference == mSourceUrl) {
            launchUrl("http://github.com/TeamDRH");
        } else if (preference == mIrcUrl) {
            launchUrl("http://webchat.freenode.net/?channels=gtab-kerneldev");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }
}
