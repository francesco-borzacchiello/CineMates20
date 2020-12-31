package it.unina.ingSw.cineMates20.view.util;

import android.util.Patterns;

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
}
