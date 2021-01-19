package it.unina.ingSw.cineMates20.view.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.AmplifyConfiguration;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.UserDB;

public class Utilities {

    public static boolean isUserNameValid(String username) {
        if(username != null) {
            if(username.trim().isEmpty())
                return false;

            if(username.trim().length() < 3)
                return false;

            Pattern whiteSpacePattern = Pattern.compile("\\s+");
            return !whiteSpacePattern.matcher(username.trim()).find();
        }
        return true;
    }

    public static boolean isPasswordValid(String password) {
        if(password != null) {
        /*  Vincoli di Cognito richiesti:
            Richiedi numeri
            Richiedi carattere speciale
            Richiedi lettere maiuscole
            Richiedi lettere minuscole

            Vincolo aggiuntivo: niente spazi bianchi nel mezzo
        */
            Pattern specialCharPattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Pattern upperCasePattern = Pattern.compile("[A-Z ]");
            Pattern lowerCasePattern = Pattern.compile("[a-z ]");
            Pattern digitCasePattern = Pattern.compile("[0-9 ]");
            Pattern whiteSpacePattern = Pattern.compile("\\s+");

            if (password.trim().length() < 8)
                return false;

            if(whiteSpacePattern.matcher(password.trim()).find())
                return false;

            if (!specialCharPattern.matcher(password).find())
                return false;

            if (!upperCasePattern.matcher(password).find())
                return false;

            if (!lowerCasePattern.matcher(password).find())
                return false;

            return digitCasePattern.matcher(password).find();
        }
        return false;
    }

    public static boolean isConfirmPasswordValid(@NotNull String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public static boolean isEmailValid(String email) {
        if (email == null) return false;

        Pattern validEmailAddressRegex =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        return validEmailAddressRegex.matcher(email.trim()).find();
    }


    public static void stampaToast(@NotNull Activity activity, String msg) {
        Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /* Metodo per determinare se l'applicazione dispone di una connessione
       Mobile o WIFI, prima di inviare qualunque richiesta internet al server. */
    public static boolean isOnline(@NotNull Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isAvailable() && activeNetwork.isConnected()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectNetwork().penaltyDialog()
                    .permitNetwork() //permit Network access
                    .build());

            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("https://clients3.google.com/generate_204") //Oppure https://aws.amazon.com/ (da testare)
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(2000);
                urlc.connect();

                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (Exception ignored) {}
        }
        return false;
    }

    public static void hideKeyboard(@NotNull Activity activity, MotionEvent event) {
        View view = activity.getCurrentFocus();
        if (view instanceof EditText) {
            View w = activity.getCurrentFocus();
            int[] screenCords = new int[2];
            w.getLocationOnScreen(screenCords);
            float x = event.getRawX() + w.getLeft() - screenCords[0];
            float y = event.getRawY() + w.getTop() - screenCords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    //Metodo che consente di svuotare il backStack prima di lanciare una nuova activity
    public static void clearBackStack(@NotNull Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    //Metodo che consente di svuotare il backStack eccetto la prima activity root
    public static void resumeBottomBackStackActivity(@NotNull Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    public static boolean checkNullActivityOrNoConnection(Activity activity) {
        if(activity == null)
            //TODO: gestire questo caso (non si può chiamare stampaToast poiché activity è null)
            //....
            return true; //null activity

        if(!Utilities.isOnline(activity)) {
            activity.runOnUiThread(() -> Utilities.stampaToast(activity, activity.getApplicationContext().getResources().getString(R.string.networkNotAvailable)));
            return true; //no connection
        }

        return false;
    }

    /**
     * Restituisce una serie di informazioni relative all'utente
     * attualmente loggato in una lista in cui:
     *   - alla prima posizione si trova il nome
     *   - alla seconda il cognome,
     *   - alla terza ed ultima posizione si trova l'username
     * Nota: nome e cognome riguardano i dati nel Database di
     *       Cognito o in quello interno, non sono quelli associati all'account social
     */
    @NotNull
    public static List<String> getCurrentUserInformations(Activity activity) {
        final List<String> informations = new ArrayList<>();
        AtomicBoolean done = new AtomicBoolean(false);

        new Thread (() -> {
            if(Amplify.Auth.getPlugins().size() == 0) {
                try {
                    Amplify.addPlugin(new AWSCognitoAuthPlugin());
                    AmplifyConfiguration config = AmplifyConfiguration.builder
                            (activity.getApplicationContext()).devMenuEnabled(false).build();
                    Amplify.configure(config, activity.getApplicationContext());
                } catch (AmplifyException e) { done.set(true); }
            }

            AuthUser user = Amplify.Auth.getCurrentUser();
            if(user != null) {
                Amplify.Auth.fetchUserAttributes(
                        attributes -> {
                            if(attributes.size() > 5) {
                                String nomeCompleto = attributes.get(4).getValue(); //In posizione 4 c'è l'informazione del nome e del cognome concatenati
                                int idx = nomeCompleto.lastIndexOf(' ');
                                String nome = nomeCompleto.substring(0, idx);
                                String cognome = nomeCompleto.substring(idx + 1);
                                informations.add(nome);
                                informations.add(cognome);
                                informations.add(attributes.get(3).getValue()); //In posizione 3 c'è l'informazione dell'username

                                done.set(true);
                                synchronized(informations) {
                                    informations.notifyAll();
                                }
                            }
                        },
                        error -> {
                            done.set(true);
                            synchronized(informations) {
                                informations.notifyAll();
                            }
                        }
                );
            }
            //TODO: da testare questo ramo dell'if
            else { //L'utente è loggato con un social, per cui i dati vanno ricercati nel DB interno
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String url = activity.getResources().getString(R.string.db_path) + "User/getById/{email}";

                String email = tryToGetFacebookEmail();
                if(email == null || email.equals(""))
                    email = tryToGetGoogleEmail(activity);

                try {
                    UserDB userDB = restTemplate.getForObject(url, UserDB.class, email);
                    informations.add(userDB.getNome());
                    informations.add(userDB.getCognome());
                    informations.add(userDB.getUsername());

                    done.set(true);
                    synchronized(informations) {
                        informations.notifyAll();
                    }
                }catch(HttpClientErrorException e){
                    done.set(true);
                    synchronized(informations) {
                        informations.notifyAll();
                    }
                }
            }
        }).start();

        while(!done.get()) {
            synchronized (informations) {
                try {
                    informations.wait();
                } catch (InterruptedException ignore) {}
            }
        }

        return informations;
    }

    @Nullable
    public static String tryToGetFacebookEmail() {
        final String[] email = new String[1];
        email[0] = null;
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();

        if(fbAccessToken == null) return null;

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                (object, response) -> {
                    try {
                        if (object.has("email"))
                            email[0] = object.getString("email");
                        else email[0] = null;
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

        return email[0];
    }

    @Nullable
    public static String tryToGetGoogleEmail(Activity activity) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);
        if (acct != null)
            return acct.getEmail();

        return null;
    }
}
