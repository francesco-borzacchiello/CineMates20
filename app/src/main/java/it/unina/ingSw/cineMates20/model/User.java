package it.unina.ingSw.cineMates20.model;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.AmplifyConfiguration;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.ingSw.cineMates20.R;
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

        AuthUser user = Amplify.Auth.getCurrentUser();
        if(user != null) { //L'utente è autenticato con Cognito
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
        else { //L'utente è loggato con un social, per cui i dati vanno ricercati nel DB interno
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
            String url = activity.getResources().getString(R.string.db_path) + "User/getById/{email}";

            email = tryToGetFacebookEmail();
            if(email == null || email.equals(""))
                email = tryToGetGoogleEmail(activity);

            try {
                Thread t = new Thread(()-> {
                    //Usa l'email social per identificare l'utente nel Database interno
                    UserDB userDB = restTemplate.getForObject(url, UserDB.class, email);
                    name = userDB.getNome();
                    surname = userDB.getCognome();
                    username = userDB.getUsername();
                });
                t.start();

                try {
                    t.join();
                }catch(InterruptedException ignore){}

            }catch(HttpClientErrorException ignore){}
        }
    }

    private static void initializeTotalNotificationNumber() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled())
                    enableNotificationTask();
            }
        }, 10000);
    }

    private static void enableNotificationTask() {
        notificationTaskIsAlive = true;
        scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        // Ricalcola il numero di notifiche totali ogni minuto
        scheduleTaskExecutor.scheduleAtFixedRate(() -> {
            //TODO: da sostituire con codice che restituisce il numero di notifiche dal database
            Random rd = new Random();
            boolean tmp = rd.nextBoolean();

            if(tmp)
                totalNotificationNumber = 99;
            else
                totalNotificationNumber = 0;
            //Log.i("TASKNOTIFICHE", "ESEGUO");
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Nullable
    private static String tryToGetFacebookEmail() {
        final String[] facebookEmail = new String[1];
        facebookEmail[0] = null;
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();

        if(fbAccessToken == null) return null;

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                (object, response) -> {
                    try {
                        if (object.has("email"))
                            facebookEmail[0] = object.getString("email");
                        else facebookEmail[0] = null;
                    } catch (JSONException ignore) {}
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);

        Thread t = new Thread(request::executeAndWait);
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return facebookEmail[0];
    }

    @Nullable
    private static String tryToGetGoogleEmail(Activity activity) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);
        if (acct != null)
            return acct.getEmail();

        return null;
    }
}