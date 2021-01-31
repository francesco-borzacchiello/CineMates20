package it.unina.ingSw.cineMates20.view.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Pattern;

import it.unina.ingSw.cineMates20.R;

public class Utilities {

    public static boolean isUserNameValid(String username) {
        if(username != null) {
            if(username.trim().isEmpty() || username.trim().length() < 3 || username.contains("@"))
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
            return true;

        if(!Utilities.isOnline()) {
            activity.runOnUiThread(() -> Utilities.stampaToast(activity, activity.getApplicationContext().getResources().getString(R.string.networkNotAvailable)));
            return true;
        }

        return false;
    }
}
