package it.unina.ingSw.cineMates20.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.FriendsActivity;
import it.unina.ingSw.cineMates20.view.activity.UserActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class UserController {
    //region Attributi
    private static UserController instance;
    private UserActivity userActivity;
    private AppCompatActivity activityParent;
    private boolean isFriendProfile;
    //endregion

    private UserController() {}

    //region getInstance() per il pattern singleton
    public static UserController getUserControllerInstance() {
        if(instance == null)
            instance = new UserController();
        return instance;
    }
    //endregion

    //region Lancio dell'Activity
    public void start(@NotNull AppCompatActivity activityParent) { //TODO: da prendere in input anche un UserDB
        Intent intent = new Intent(activityParent, UserActivity.class);
        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        this.activityParent = activityParent;
        isFriendProfile = (activityParent instanceof FriendsActivity);
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setUserActivity(@NotNull UserActivity userActivity) {
        this.userActivity = userActivity;
        hideProgressBar();
    }
    //endregion

    public void hideProgressBar() {
        if(activityParent != null) {
            if(activityParent instanceof FriendsActivity)
                FriendsController.getFriendsControllerInstance().hideFriendsActivityProgressBar();
            //else if(activityParent instanceof SearchUsersActivity) ...
        }
    }

    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(itemId == android.R.id.home) {
                userActivity.finish();
                userActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };
    }

    public boolean isFriendProfile() {
        return isFriendProfile;
    }

    public View.OnClickListener getRemoveFriendOnClickListener() {
        return v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(userActivity)
                    .setMessage("Rimuovere " + "Username" + " dalla lista di amici?")
                    .setCancelable(false)
                    .setPositiveButton("Si", (dialog, id) -> {
                        if (Utilities.checkNullActivityOrNoConnection(userActivity)) return;

                        FriendsController.getFriendsControllerInstance().removeSelectedFriend(); //Rimozione grafica, non reale
                        //TODO: comunicazione col database per confermare rimozione amicizia

                        userActivity.startActivity(new Intent(userActivity, UserActivity.class));
                        userActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        isFriendProfile = false;
                        userActivity.finish();
                    })
                    .setNegativeButton("No", null).show();

            alertDialog.setOnKeyListener((arg0, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                    alertDialog.dismiss();
                return true;
            });
        };
    }

    public View.OnClickListener getAddFriendOnClickListener() {
        return v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(userActivity)
                    .setMessage("Inviare richiesta di amicizia a " + "Username" + "?")
                    .setCancelable(false)
                    .setPositiveButton("Si", (dialog, id) -> {
                        if (Utilities.checkNullActivityOrNoConnection(userActivity)) return;

                        FriendsController.getFriendsControllerInstance().removeSelectedFriend(); //Rimozione grafica, non reale
                        //TODO: comunicazione col database per inviare richiesta di amicizia

                        userActivity.disableAddFriendButton();

                        Utilities.stampaToast(userActivity, "Richiesta di amicizia inviata");
                    })
                    .setNegativeButton("No", null).show();

            alertDialog.setOnKeyListener((arg0, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                    alertDialog.dismiss();
                return true;
            });
        };
    }

    public View.OnClickListener getJoinedMoviesOnClickListener() {
        return v -> JoinedMoviesController.getJoinedMoviesControllerInstance().start(userActivity);
    }

    public MenuItem.OnMenuItemClickListener getReportItemOnClickListener() {
        return menuItem -> {
            if(activityParent instanceof FriendsActivity) {
                UserDB user = FriendsController.getFriendsControllerInstance().getLastClickedUser();
                ReportController.getReportControllerInstance().startUserReport(userActivity, user);
            }
            /*else if(activityParent instanceof SearchUserActivity) {
                UserDB user = SearchUserController.getSearchUserControllerInstance().getLastClickedUser();
                ReportController.getReportControllerInstance().startUserReport(userActivity, user);
            }*/

            return true;
        };
    }
}
