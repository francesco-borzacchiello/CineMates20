package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.PersonalProfileActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class PersonalProfileController {
    //region Attributi
    private static PersonalProfileController instance;
    private PersonalProfileActivity personalProfileActivity;
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
    public void start(@NotNull Activity activityParent) {
        Intent intent = new Intent(activityParent, PersonalProfileActivity.class);
        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //endregion

    //Restituisce un listener per le icone della toolbar in PersonalProfileActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(itemId == android.R.id.home)
                personalProfileActivity.openDrawerLayout();
            else if(itemId == R.id.notificationItem &&
                    !Utilities.checkNullActivityOrNoConnection(personalProfileActivity)) {
                NotificationController.getNotificationControllerInstance().start(personalProfileActivity);
            }
        };
    }
}
