package it.unina.ingSw.cineMates20.view.util;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;

import java.io.Serializable;

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

    /* Classe utilizzata per incapsulare un Runnable all'interno di un intent Bundle
       Nota: la classe è statica per permettere la "Serializzazione", ma attraverso l'instanziazione ad ogni uso,
       è possibile ottenere istanze indipendenti tra loro, in modo tale da rendere i Runnable passati alla classe indipendenti. */
    public static class Srunnable implements Serializable {
        Runnable r;
        public Srunnable(Runnable r) { this.r = r; }

        public Runnable getRunnable() { return r; }
    }

    public static void stampaToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
}
