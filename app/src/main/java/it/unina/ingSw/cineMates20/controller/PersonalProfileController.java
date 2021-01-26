package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.PersonalProfileActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class PersonalProfileController {
    //region Attributi
    private static PersonalProfileController instance;
    private PersonalProfileActivity personalProfileActivity;
    private boolean isMyProfile;
    private UserDB genericUser;
    //endregion

    private PersonalProfileController() {}

    //region getInstance() per il pattern singleton
    public static PersonalProfileController getPersonalProfileControllerInstance() {
        if(instance == null)
            instance = new PersonalProfileController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setPersonalProfileActivity(@NotNull PersonalProfileActivity personalProfileActivity) {
        this.personalProfileActivity = personalProfileActivity;
    }
    //endregion

    //region Lancio dell'Activity
    public void startPersonalProfile(@NotNull Activity activityParent) {
        Intent intent = new Intent(activityParent, PersonalProfileActivity.class);
        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        isMyProfile = true;
    }

    public void startGenericProfile(@NotNull Activity activityParent, UserDB genericUser) {
        Intent intent = new Intent(activityParent, PersonalProfileActivity.class);
        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        isMyProfile = false;
        this.genericUser = genericUser;
    }
    //endregion

    //Restituisce un listener per le icone della toolbar in PersonalProfileActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(isMyProfile) {
                if(itemId == android.R.id.home)
                    personalProfileActivity.openDrawerLayout();
                else if(itemId == R.id.notificationItem &&
                        !Utilities.checkNullActivityOrNoConnection(personalProfileActivity)) {
                    NotificationsController.getNotificationControllerInstance().start(personalProfileActivity);
                }
            }
            else if(itemId == android.R.id.home) {
                personalProfileActivity.finish();
                personalProfileActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };
    }

    public boolean isMyProfile() {
        return isMyProfile;
    }

    public UserDB getGenericUser() {
        return genericUser;
    }
}
