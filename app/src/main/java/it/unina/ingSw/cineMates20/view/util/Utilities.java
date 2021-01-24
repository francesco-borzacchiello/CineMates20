package it.unina.ingSw.cineMates20.view.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    public static boolean isOnline() {
        boolean [] ret = new boolean[1];

        Thread t = new Thread(()-> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("8.8.8.8", 53), 3000);
                socket.close();
                ret[0] = true;
            } catch (IOException e) {
                ret[0] = false;
            }
        });
        t.start();

        try {
            t.join();
        }catch (InterruptedException ignore) {}

        return ret[0];
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

        if(!Utilities.isOnline()) {
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
     *   - alla terza l'username,
     *   - alla quarta ed ultima posizione si trova l'email
     * Nota: nome e cognome riguardano i dati nel Database di
     *       Cognito o in quello interno, non sono quelli associati all'account social
     */
    @NotNull
    public static List<String> getCurrentUserInformations(Activity activity) {
        final List<String> informations = new ArrayList<>();
        if(Amplify.Auth.getPlugins().size() == 0) {
            try {
                Amplify.addPlugin(new AWSCognitoAuthPlugin());
                AmplifyConfiguration config = AmplifyConfiguration.builder
                        (activity.getApplicationContext()).devMenuEnabled(false).build();
                Amplify.configure(config, activity.getApplicationContext());
            } catch (AmplifyException ignore) {}
        }

        AuthUser user = Amplify.Auth.getCurrentUser();
        if(user != null) {
            AtomicBoolean done = new AtomicBoolean(false);
            Thread t = new Thread(()->
                Amplify.Auth.fetchUserAttributes(
                        attributes -> {
                            Log.i("UtenteLoggato", attributes.size()+"");
                            if(attributes.size() > 5) {
                                String nome = "", cognome = "", username ="", email = "";
                                for(AuthUserAttribute attr: attributes) {
                                    Log.i("Attr", attr.getValue());
                                    Log.i("Attr", attr.getKey()+"");

                                    if(attr.getKey().toString().contains("family_name"))
                                        cognome = attr.getValue();
                                    else if(attr.getKey().toString().contains("given_name"))
                                        nome = attr.getValue();
                                    else if(attr.getKey().toString().contains("email") && !attr.getKey().toString().contains("verified"))
                                        email = attr.getValue();
                                    else if(attr.getKey().toString().contains("preferred_username"))
                                        username = attr.getValue();
                                }
                                //Log.i("UtenteLoggato", "Sono nell'if");
                                //String nomeCompleto = attributes.get(4).getValue(); //In posizione 4 c'è l'informazione del nome e del cognome concatenati
                                //Log.i("UtenteLoggato", "nome completo: "+nomeCompleto);
                                //int idx = nomeCompleto.lastIndexOf(' ');
                                //Log.i("UtenteLoggato", "nome completo: "+nomeCompleto);
                                //nome = nomeCompleto.substring(0, idx);
                                //cognome = nomeCompleto.substring(idx + 1);
                                Log.i("UtenteLoggato", "informations.add()");
                                informations.add(nome);
                                informations.add(cognome);
                                informations.add(username);
                                informations.add(email);
                                Log.i("Attr", nome+" "+cognome+" "+username+" "+email);
                                done.set(true);
                                Log.i("UtenteLoggato", "Prima di synchronized.notify");
                                synchronized(done){
                                    Log.i("UtenteLoggato", "Prima notify");
                                    done.notifyAll();
                                    Log.i("UtenteLoggato", "Dopo notify");
                                }
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

            String[] email = new String[1];
            email[0] = tryToGetFacebookEmail();
            if(email[0] == null || email[0].equals(""))
                email[0] = tryToGetGoogleEmail(activity);

            try {
                Thread t = new Thread(()-> {
                    //Usa l'email social per identificare l'utente nel Database interno
                    UserDB userDB = restTemplate.getForObject(url, UserDB.class, email[0]);
                    informations.add(userDB.getNome());
                    informations.add(userDB.getCognome());
                    informations.add(userDB.getUsername());
                    informations.add(email[0]);
                });
                t.start();

                try {
                    t.join();
                }catch(InterruptedException ignore){}

            }catch(HttpClientErrorException ignore){}
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
