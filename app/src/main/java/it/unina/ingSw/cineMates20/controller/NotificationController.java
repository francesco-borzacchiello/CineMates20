package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.model.UserHttpRequests;
import it.unina.ingSw.cineMates20.view.activity.NotificationActivity;
import it.unina.ingSw.cineMates20.view.adapter.NotificationPagerAdapter;
import it.unina.ingSw.cineMates20.view.adapter.PendingFriendsRequestsAdapter;
import it.unina.ingSw.cineMates20.view.adapter.ReportNotificationAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class NotificationController {
    private static NotificationController instance;
    private NotificationActivity notificationActivity;
    private PendingFriendsRequestsAdapter friendsRequestsAdapter;

    //region Costruttore
    private NotificationController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, NotificationActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //endregion

    //region getInstance() per il pattern singleton
    public static NotificationController getNotificationControllerInstance() {
        if(instance == null)
            instance = new NotificationController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setNotificationActivity(@NotNull NotificationActivity notificationActivity) {
        this.notificationActivity = notificationActivity;
    }

    public NotificationPagerAdapter getViewPager2Adapter() {
        return new NotificationPagerAdapter(notificationActivity);
    }
    //endregion

    public void initializeFriendRequestsNotificationAdapter() {
        ArrayList<UserDB> users = new ArrayList<>(UserHttpRequests.getInstance().
                getAllPendingFriendRequests(User.getLoggedUser(notificationActivity).getEmail()));

        ArrayList<Runnable> usersLayoutListeners = new ArrayList<>(),
                            acceptRequestListeners = new ArrayList<>(),
                            rejectRequestListeners = new ArrayList<>();

        for(UserDB user: users) {
            usersLayoutListeners.add(getUserLayoutListener(user));
            acceptRequestListeners.add(getAcceptRequestListener(user));
            rejectRequestListeners.add(getRejectRequestListener(user));
        }

        friendsRequestsAdapter = new PendingFriendsRequestsAdapter
                (notificationActivity, users, usersLayoutListeners, acceptRequestListeners, rejectRequestListeners);

        friendsRequestsAdapter.setHasStableIds(true);
        notificationActivity.setFriendsNotificationsRecyclerView(friendsRequestsAdapter);
    }

    //TODO: da modificare con segnalazioni reali dopo aver completato applicativo server
    public void initializeReportNotificationAdapter() {
        ArrayList<String> reportSubjects = new ArrayList<>(),
                          reportOutcomes = new ArrayList<>();
        ArrayList<Runnable> removeNotificationListeners = new ArrayList<>();

        //Popolamento temporaneo con dati fittizzi:
        for(int i = 0; i<20; i++) {
            reportSubjects.add("Segnalazione Film X:");
            reportOutcomes.add("La tua segnalazione è stata accettata.");
            removeNotificationListeners.add(getRemoveReportNotificationListener(0));
        }

        ReportNotificationAdapter reportNotificationAdapter = new ReportNotificationAdapter
                (notificationActivity, reportSubjects, reportOutcomes, removeNotificationListeners);
        reportNotificationAdapter.setHasStableIds(true);
        notificationActivity.setReportNotificationsRecyclerView(reportNotificationAdapter);
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getUserLayoutListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationActivity)) return;

            UserController.getUserControllerInstance().start(notificationActivity, user);
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getAcceptRequestListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationActivity)) return;

            if(UserHttpRequests.getInstance().confirmFriendRequest
                    (User.getLoggedUser(notificationActivity).getEmail(), user.getEmail())) {
                notificationActivity.decreaseFriendsNotificationsBadgeNumber();
                friendsRequestsAdapter.deleteItem(user);

            }
            else
                notificationActivity.runOnUiThread(()-> Utilities.stampaToast(notificationActivity, "Si è verificato un errore.\nRiprova più tardi."));
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getRejectRequestListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationActivity)) return;

            if(UserHttpRequests.getInstance().removeFriend
                    (User.getLoggedUser(notificationActivity).getEmail(), user.getEmail())) {
                notificationActivity.decreaseFriendsNotificationsBadgeNumber();
                friendsRequestsAdapter.deleteItem(user);
            }
            else
                notificationActivity.runOnUiThread(()-> Utilities.stampaToast(notificationActivity, "Si è verificato un errore.\nRiprova più tardi."));
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getRemoveReportNotificationListener(int reportId) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationActivity)) return;

            notificationActivity.decreaseReportNotificationsBadgeNumber();

            //TODO: Rimuovi questa segnalazione dal database
        };
    }

    public void showEmptyFriendsNotificationPage(boolean show) {
        if(notificationActivity != null)
            notificationActivity.showEmptyFriendsNotificationPage(show);
    }

    public void showEmptyReportsNotificationPage(boolean show) {
        if(notificationActivity != null)
            notificationActivity.showEmptyReportsNotificationPage(show);
    }

    public boolean isInternetAvailable() {
        if(notificationActivity != null)
            return !Utilities.checkNullActivityOrNoConnection(notificationActivity);
        return false;
    }
}
