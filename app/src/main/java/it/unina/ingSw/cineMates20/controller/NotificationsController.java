package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.NotificationsActivity;
import it.unina.ingSw.cineMates20.view.adapter.NotificationsPagerAdapter;
import it.unina.ingSw.cineMates20.view.adapter.PendingFriendsRequestsAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class NotificationsController {
    private static NotificationsController instance;
    private NotificationsActivity notificationsActivity;

    //region Costruttore
    private NotificationsController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, NotificationsActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //endregion

    //region getInstance() per il pattern singleton
    public static NotificationsController getNotificationControllerInstance() {
        if(instance == null)
            instance = new NotificationsController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setNotificationsActivity(@NotNull NotificationsActivity notificationsActivity) {
        this.notificationsActivity = notificationsActivity;
    }

    public NotificationsPagerAdapter getViewPager2Adapter() {
        return new NotificationsPagerAdapter(notificationsActivity);
    }
    //endregion

    //TODO: da modificare con utenti reali dopo aver completato applicativo server
    public void initializeNotificationsFriendRequestsAdapter() {
        ArrayList<UserDB> users = new ArrayList<>();

        ArrayList<Runnable> usersLayoutListeners = new ArrayList<>(),
                            acceptRequestListeners = new ArrayList<>(),
                            rejectRequestListeners = new ArrayList<>();

        //Popolamento temporaneo con dati fittizzi:
        for(int i = 0; i<20; i++) {
            users.add(new UserDB("Username", "Nome", "Cognome", "test@gmail.com", "utente"));
        }

        for(UserDB user: users) {
            usersLayoutListeners.add(getUserLayoutListener(user));
            acceptRequestListeners.add(getAcceptRequestListener(user));
            rejectRequestListeners.add(getRejectRequestListener(user));
        }

        PendingFriendsRequestsAdapter adapter = new PendingFriendsRequestsAdapter
                (notificationsActivity, users, usersLayoutListeners, acceptRequestListeners, rejectRequestListeners);

        adapter.setHasStableIds(true);
        notificationsActivity.setFriendsNotificationsRecyclerView(adapter);
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getUserLayoutListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationsActivity)) return;

            PersonalProfileController.getPersonalProfileControllerInstance().startGenericProfile(notificationsActivity, user);
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getAcceptRequestListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationsActivity)) return;

            notificationsActivity.decreaseFriendsNotificationsBadgeNumber();

            //TODO: Aggiungi "user" agli amici nel database
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getRejectRequestListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationsActivity)) return;

            notificationsActivity.decreaseFriendsNotificationsBadgeNumber();

            //TODO: Rimuovi "user" dagli amici nel database
        };
    }

    public void showEmptyNotificationsPage() {
        if(notificationsActivity != null)
            notificationsActivity.showEmptyNotificationsPage();
    }
}
