package it.unina.ingSw.cineMates20.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.SettingsActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsActivity settingsActivity;

    public SettingsFragment() {}

    public SettingsFragment(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (settingsActivity == null) return;

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(settingsActivity);

        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
            if (key.equals("adult_filter")) {
                //prefs.getString(key,"Error") sarà "Abilita" se il filtro è abilitato
                settingsActivity.filterPreferencesChanged(prefs.getString(key,"Error").equals("Abilita"));
            }
            else if(key.equals("sync_notification")) {
                //prefs.getBoolean(key, false) sarà true se il filtro è abilitato
                settingsActivity.notificationPreferencesChanged(prefs.getBoolean(key, false));
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }
}