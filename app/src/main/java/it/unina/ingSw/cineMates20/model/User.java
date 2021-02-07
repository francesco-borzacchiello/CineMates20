package it.unina.ingSw.cineMates20.model;

import android.app.Activity;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.AmplifyConfiguration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.ingSw.cineMates20.controller.SettingsController;

/**
 * Classe che memorizza le informazioni di base dell'utente loggato
 */
public class User {

    private static String name;
    private static String surname;
    private static String email;
    private static String username;
    private static ScheduledExecutorService scheduleTaskExecutor;
    private static int totalNotificationNumber;
    private static boolean notificationTaskIsAlive;

    @NotNull
    @Contract("_ -> new")
    public static UserDB getLoggedUser(Activity activity) {
        if(someFieldsAreNull())
            initializeUserInstance(activity);
        return new UserDB(username, name, surname, email, "utente");
    }

    /* A causa del fatto che alcune activity possono chiamare questa classe prima che l'utente sia loggato,
       grazie a tale metodo, sappiamo che è necessario riprovare a creare l'istanza utente, che ha il
       principale scopo di rappresentare un utente loggato. */
    private static boolean someFieldsAreNull() {
        return name == null || surname == null || email == null || email.equals("") || username == null;
    }

    public static void deleteUserInstance() {
        if(notificationTaskIsAlive) {
            scheduleTaskExecutor.shutdownNow();
            notificationTaskIsAlive = false;
        }
        name = null; //Basta uno solo dei campi della classe
    }

    public static int getTotalUserNotificationCount() {
        return totalNotificationNumber;
    }

    public static void enableNotificationFilter(boolean enabled) {
        if(enabled && !notificationTaskIsAlive)
            enableNotificationTask();
        else if(!enabled && notificationTaskIsAlive) {
            scheduleTaskExecutor.shutdownNow();
            notificationTaskIsAlive = false;
        }
    }

    @Nullable
    public static String getUserProfilePictureUrl() {
        if(email != null)
            return S3Manager.getProfilePictureUrl(email);
        return null;
    }

    private static void initializeUserInstance(Activity activity) {
        initializeTotalNotificationNumber();

        if(Amplify.Auth.getPlugins().size() == 0) {
            try {
                Amplify.addPlugin(new AWSCognitoAuthPlugin());
                AmplifyConfiguration config = AmplifyConfiguration.builder
                        (activity.getApplicationContext()).devMenuEnabled(false).build();
                Amplify.configure(config, activity.getApplicationContext());
            } catch (AmplifyException ignore) {}
        }

        AuthUser authuser = Amplify.Auth.getCurrentUser();
        if(authuser != null) { //L'utente è autenticato con Cognito
            AtomicBoolean done = new AtomicBoolean(false);
            Thread t = new Thread(()->
                Amplify.Auth.fetchUserAttributes(
                    attributes -> {
                        Log.i("UtenteLoggato", attributes.size()+"");
                        for(AuthUserAttribute attr: attributes) {
                            Log.i("Attr", attr.getValue());
                            Log.i("Attr", attr.getKey()+"");

                            if(attr.getKey().toString().contains("family_name"))
                                surname = attr.getValue();
                            else if(attr.getKey().toString().contains("given_name"))
                                name = attr.getValue();
                            else if(attr.getKey().toString().contains("email") && !attr.getKey().toString().contains("verified"))
                                email = attr.getValue();
                            else if(attr.getKey().toString().contains("preferred_username"))
                                username = attr.getValue();
                        }

                        done.set(true);

                        synchronized(done){
                            Log.i("UtenteLoggato", "Prima notify");
                            done.notifyAll();
                            Log.i("UtenteLoggato", "Dopo notify");
                        }
                    },
                    error -> {}
                ));
            t.start();

            while(!done.get()){
                synchronized(done){
                    try {
                        Log.i("UtenteLoggato", "Prima wait");
                        done.wait();
                        Log.i("UtenteLoggato", "Dopo wait");
                    }catch (InterruptedException ignore){}
                }
            }
        }
        else { //L'utente è autenticato con un social, occorre interrogare il DB interno
            UserDB user = UserHttpRequests.getInstance().getSocialLoggedUser(activity);
            if(user != null) {
                name = user.getNome();
                surname = user.getCognome();
                username = user.getUsername();
                email = user.getEmail();
            }
        }
    }

    private static void initializeTotalNotificationNumber() {
        if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    enableNotificationTask();
                }
            }, 10000);
        }
        else { //Si sincronizza solo al primo avvio dell'applicazione
            totalNotificationNumber =
                    UserHttpRequests.getInstance().getAllPendingFriendRequests(email).size()
                    + ReportHttpRequests.getInstance().getAllMoviesReports(email).size()
                    + ReportHttpRequests.getInstance().getAllUsersReports(email).size();
        }
    }

    private static void enableNotificationTask() {
        notificationTaskIsAlive = true;
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

        //Ricalcola il numero di notifiche totali ogni minuto
        scheduleTaskExecutor.scheduleAtFixedRate(() ->
                totalNotificationNumber =
                        UserHttpRequests.getInstance().getAllPendingFriendRequests(email).size()
                        + ReportHttpRequests.getInstance().getAllMoviesReports(email).size()
                        + ReportHttpRequests.getInstance().getAllUsersReports(email).size(),
                0, 15, TimeUnit.SECONDS);
    }
}