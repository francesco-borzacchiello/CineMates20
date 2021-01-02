package it.unina.ingSw.cineMates20.view.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.util.Patterns;
import android.widget.Toast;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utilities {

    public static boolean isUserNameValid(String username) {
        return username != null && !username.trim().isEmpty()
                && username.trim().length() > 3;
        //TODO: aggiungere && !username.isUsed() tramite un metodo di Amplify
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() >= 8;
        /* TODO: aggiungere gli altri vincoli di Cognito richiesti:
            Richiedi numeri
            Richiedi carattere speciale
            Richiedi lettere maiuscole
            Richiedi lettere minuscole
        */
    }

    public static boolean isConfirmPasswordValid(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public static boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        //TODO: aggiungere && !email.isUsed() tramite un metodo di Amplify OR email.isUsed() && email.isPending()
    }

    /**
     *  Classe utilizzata per incapsulare un Runnable all'interno di un intent Bundle
     *  Nota: la classe è statica per permettere la "Serializzazione", ma attraverso l'instanziazione ad ogni uso,
     *  è possibile ottenere istanze indipendenti tra loro, in modo tale da rendere i Runnable passati alla classe indipendenti.
     */
    public static class Srunnable implements Serializable {
        Runnable runnable;
        public Srunnable(Runnable runnable) { this.runnable = runnable; }

        public Runnable getRunnable() { return runnable; }
    }

    public static void stampaToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /* Metodo per determinare se l'applicazione dispone di una connessione
       Mobile o WIFI, prima di inviare qualunque richiesta internet al server. */
    public static boolean isOnline(Context context) {
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
}
