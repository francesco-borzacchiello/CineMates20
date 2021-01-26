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

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.ingSw.cineMates20.R;

/**
 * Classe che memorizza le informazioni di base dell'utente loggato
 */
public class User {

    private static User instance;
    private static String name;
    private static String surname;
    private static String email;
    private static String username;

    private User(){}

    public static User getUserInstance(Activity activity) {
        if(instance == null || someFieldsAreNull()) {
            instance = new User();
            initializeUserInstance(activity);
        }
        return instance;
    }

    /* A causa del fatto che alcune activity possono chiamare questa classe prima che l'utente sia loggato,
       grazie a tale metodo, sappiamo che è necessario riprovare a creare l'istanza singleton, che ha il
       principale scopo di rappresentare un utente loggato. */
    private static boolean someFieldsAreNull() {
        return name == null || surname == null || email == null || email.equals("") || username == null;
    }

    public static void deleteUserInstance() {
        instance = null;
    }

    /* Potrebbe restituire anche sempre lo stesso UserDB, ma se poi
       qualcuno ci facesse dei set, l'oggetto verrebbe invalidato */
    public UserDB getLoggedUser() {
        return new UserDB(username, name, surname, email, "utente");
    }

    private static void initializeUserInstance(Activity activity) {
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