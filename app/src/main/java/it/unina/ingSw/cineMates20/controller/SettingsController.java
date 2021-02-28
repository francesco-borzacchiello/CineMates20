package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.activity.SettingsActivity;

public class SettingsController {
    private static SettingsController instance;
    private SettingsActivity settingsActivity;
    private boolean enableNotificationSync,
                    enableSearchMovieFilter;
    private static AppCompatActivity activity;

    private SettingsController() {
        if(activity != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            enableNotificationSync = preferences.getBoolean("sync_notification", false);
            enableSearchMovieFilter = preferences.getString("adult_filter", "Disabilita").equals("Abilita");
        }
        else { //Valori di default
            enableNotificationSync = true;
            //enableSearchMovieFilter = false; //Pre-inizializzato già a false
        }
    }

    public static SettingsController getSettingsControllerInstance() {
        if(instance == null)
            instance = new SettingsController();

        return instance;
    }

    public static void setSettingsControllerContextActivity(AppCompatActivity activityIn) {
        activity = activityIn;
    }

    public void start(Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void setSettingsActivity(@NotNull SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    //Restituisce un listener per le icone della toolbar in PersonalProfileActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(itemId == android.R.id.home)
                settingsActivity.openDrawerLayout();
        };
    }

    public void setFilterPreference(boolean enabled) {
        enableSearchMovieFilter = enabled;
    }

    public void setNotificationPreference(boolean enable) {
        User.enableNotificationFilter(enable);
        enableNotificationSync = enable;
    }

    public boolean isSearchMovieFilterEnabled() {
        return enableSearchMovieFilter;
    }

    public boolean isNotificationSyncEnabled() {
        return enableNotificationSync;
    }
}
