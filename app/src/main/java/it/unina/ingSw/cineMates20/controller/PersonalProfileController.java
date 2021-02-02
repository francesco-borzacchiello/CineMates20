package it.unina.ingSw.cineMates20.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.S3Manager;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.activity.PersonalProfileActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class PersonalProfileController {
    //region Attributi
    private static PersonalProfileController instance;
    private PersonalProfileActivity personalProfileActivity;
    private final int PICK_IMAGE = 1;
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

    public int getPickImageCode() {
        return PICK_IMAGE;
    }

    public View.OnClickListener getEditProfilePictureOnClickListener() {
        return v -> ActivityCompat.requestPermissions
                (personalProfileActivity, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, PICK_IMAGE);
    }

    public void launchGalleryIntentPicker() {
        if(personalProfileActivity == null) return;

        Intent gallery = new Intent();
        gallery.setType("image/*");

        gallery.setAction(Intent.ACTION_GET_CONTENT);
        personalProfileActivity.startActivityForResult
                (Intent.createChooser(gallery, "Seleziona l'immagine del profilo"), PICK_IMAGE);
    }

    public void uploadNewProfilePicture() {
        if(personalProfileActivity != null && personalProfileActivity.getProfileImageUri() != null)
            S3Manager.uploadImage(personalProfileActivity,
                                  personalProfileActivity.getProfileImageUri(),
                                  User.getLoggedUser(personalProfileActivity).getEmail());
    }
}
