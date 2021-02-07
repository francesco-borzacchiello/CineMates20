package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.model.ReportHttpRequests;
import it.unina.ingSw.cineMates20.model.ReportMovieDB;
import it.unina.ingSw.cineMates20.model.ReportUserDB;
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
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;

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

    public void initializeReportNotificationAdapter() {
        ArrayList<String> reportSubjects = new ArrayList<>(),
                          reportOutcomes = new ArrayList<>();
        ArrayList<Runnable> removeNotificationListeners = new ArrayList<>();

        String userEmail = User.getLoggedUser(notificationActivity).getEmail();
        List<ReportMovieDB> reportedMovies = ReportHttpRequests.getInstance().
                getAllMoviesReports(userEmail);

        List<ReportUserDB> reportedUsers = ReportHttpRequests.getInstance().
                getAllUsersReports(userEmail);

        String subject, outcome = "";

        for(ReportMovieDB reportedMovie: reportedMovies) {
            subject = "Segnalazione Film:\n" + getMovieTitleById(reportedMovie.getFKFilmSegnalato().intValue());
            reportSubjects.add(subject);

            switch(reportedMovie.getEsitoSegnalazione()) {
                case "Approvata": {
                    outcome = "La tua segnalazione è stata approvata.";
                    break;
                }
                case "Rigettata": {
                    outcome = "Abbiamo ritenuto che la tua segnalazione non violasse i nostri termini di servizio.";
                    break;
                }
                case "Oscurata": {
                    outcome = "Il contenuto della tua segnalazione è stato oscurato.";
                    break;
                }
            }

            reportOutcomes.add(outcome);
            reportedMovie.setFKUtenteSegnalatore(userEmail);
            removeNotificationListeners.add(getRemoveMovieReportNotificationListener(reportedMovie));
        }

        for(ReportUserDB reportedUser: reportedUsers) {
            subject = "Segnalazione Utente:\n" + reportedUser.getFKUtenteSegnalato();
            reportSubjects.add(subject);

            switch(reportedUser.getEsitoSegnalazione()) {
                case "Approvata": {
                    outcome = "La tua segnalazione è stata approvata.";
                    break;
                }
                case "Rigettata": {
                    outcome = "Abbiamo ritenuto che la tua segnalazione non violasse i nostri termini di servizio.";
                    break;
                }
                case "Oscurata": {
                    outcome = "L'utente è stato oscurato.";
                    break;
                }
            }

            reportOutcomes.add(outcome);
            reportedUser.setFKUtenteSegnalatore(userEmail);
            removeNotificationListeners.add(getRemoveUserReportNotificationListener(reportedUser));
        }

        ReportNotificationAdapter reportNotificationAdapter = new ReportNotificationAdapter
                (notificationActivity, reportSubjects, reportOutcomes, removeNotificationListeners);
        reportNotificationAdapter.setHasStableIds(true);
        notificationActivity.setReportNotificationsRecyclerView(reportNotificationAdapter);
    }

    @Nullable
    private String getMovieTitleById(int id) {
        String[] title = new String[1];

        Thread t = new Thread(()-> {
            TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(TMDB_API_KEY));
            MovieDb movie =  tmdbMovies.getMovie(id, "it");

            if(movie.getTitle() != null)
                title[0] = movie.getTitle();
            else {
                title[0] = tmdbMovies.getMovie(id, "en").getTitle();
                if(title[0] == null)
                    title[0] = movie.getOriginalTitle();
            }
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return title[0];
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
    private Runnable getRemoveMovieReportNotificationListener(ReportMovieDB movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationActivity)) return;

            if(!ReportHttpRequests.getInstance().updateUserDeleteMovieNotification(movie))
                Utilities.stampaToast(notificationActivity, "Si è verificato un errore\nnell'eliminazione della notifica.");

            notificationActivity.decreaseReportNotificationsBadgeNumber();
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getRemoveUserReportNotificationListener(ReportUserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(notificationActivity)) return;

            if(!ReportHttpRequests.getInstance().updateUserDeleteUserNotification(user))
                Utilities.stampaToast(notificationActivity, "Si è verificato un errore\nnell'eliminazione della notifica.");

            notificationActivity.decreaseReportNotificationsBadgeNumber();
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
