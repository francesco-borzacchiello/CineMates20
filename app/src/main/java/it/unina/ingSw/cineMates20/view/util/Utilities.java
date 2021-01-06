package it.unina.ingSw.cineMates20.view.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import org.apache.commons.validator.routines.EmailValidator;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

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

        //TODO: aggiungere && !username.isUsed() tramite un metodo di Amplify
        //Fare signIn() con username e password = "123" e catch di UserNotFoundException e UserNotConfirmedException
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
        if(email != null) {
            return EmailValidator.getInstance().isValid(email);
        }
        return false;
        //TODO: aggiungere && !email.isUsed() tramite un metodo di Amplify OR (email.isUsed() && email.isPending())
        //Fare signIn() con email e password = "123" e catch di UserNotFoundException e UserNotConfirmedException
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
                urlc.setConnectTimeout(1500);
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
    //TODO: da testare
    public static void clearBackStack(@NotNull Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}
