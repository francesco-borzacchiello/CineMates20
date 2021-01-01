package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.amplifyframework.core.Amplify;

import java.io.Serializable;

import it.unina.ingSw.cineMates20.view.activity.LoginActivity;
import it.unina.ingSw.cineMates20.view.util.InternetStatus;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class LoginController {

    public LoginController() {}

    public void start(Activity activityParent) {
        startLoginActivity(activityParent);
    }

    private void startLoginActivity(Activity activityParent){

        //Runnable per il listener sulla pressione del pulsante Login
        Runnable r = (Runnable & Serializable)() -> {
            //Credenziali di prova:
            //username: carmineG
            //password: Carmine_97
            //if(Utilities.internetIsAvaiable())
            try {
                //TODO: Aggiungere logica login in setNextStep(), se il login va a buon fine (result.isSignInComplete() == true), reindirizzare alla Home, altrimenti mostrare errore a schermo
                //Nota: in caso di wifi spento, non possiamo trovarci qui.
                Amplify.Auth.signIn(
                        "carmineG", //TODO: sostituire con credenziali date in input
                        "Carmine_97",
                        result -> Log.i("login", "sto provando a loggarmi"),  //setNextStep(result.isSignInComplete())
                        error -> Log.e("AuthQuickstart", error.toString())
                );
            } catch (Exception error) {
                Log.e("AuthQuickstart", "Could not initialize Amplify", error);
            }
        };

        Intent intent;
        intent = new Intent(activityParent, LoginActivity.class);
        intent.putExtra("Srunnable", new Utilities.Srunnable(r));
        activityParent.startActivity(intent);
    }

    //TODO: Se il login è fallito allora mostra errori a schermo, altrimenti passa alla prossima activity, chiama clearBackStack() e infine finish()
    //Nota: questo metodo sembra venga chiamato soltanto dopo il termine del corpo di loginButton.setOnClickListener()
    public void setNextStep(boolean signIn) {
        //...
    }

    public static boolean isConnectedToInternet(Context context) {
        return InternetStatus.getInstance().isOnline(context);
    }

    public static void stampaMessaggioToast(Context c, String msg) {
        Utilities.stampaToast(c, msg);
    }
}

/* VECCHIO CODICE TEMPORANEO
package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.view.activity.LoginActivity;
import it.unina.ingSw.cineMates20.view.util.InternetStatus;

public class LoginController {

    LoginActivity loginActivity;

    public LoginController() {
        this.loginActivity = new LoginActivity();
    }

    public void start(Activity activityParent) {
        startLoginActivity(activityParent);
        prepareEventHandlerForLoginButton();
    }

    private void startLoginActivity(Activity activityParent){
        Intent intent;
        intent = new Intent(activityParent, LoginActivity.class);
        activityParent.startActivity(intent);
    }

    //Credenziali di prova:
    //username: carmineG
    //password: Carmine_97
    private void prepareEventHandlerForLoginButton() {
        loginActivity.setEventForLoginClick(() -> {
            if(internetIsAvaiable())
                try {
                    //TODO: Aggiungere logica login in setNextStep(), se il login va a buon fine (result.isSignInComplete() == true), reindirizzare alla Home, altrimenti mostrare errore a schermo
                    //Nota: in caso di wifi spento, non possiamo trovarci qui.
                    Amplify.Auth.signIn(
                            "carmineG", //TODO: sostituire con credenziali date in input
                            "Carmine_97",
                            result -> Log.i("login", "sto provando a loggarmi"),  //setNextStep(result.isSignInComplete())
                            error -> Log.e("AuthQuickstart", error.toString())
                    );
                } catch (Exception error) {
                    Log.e("AuthQuickstart", "Could not initialize Amplify", error);
                }
        });
    }

    //TODO: Se il login è fallito allora mostra errori a schermo, altrimenti passa alla prossima activity, chiama clearBackStack() e infine finish()
    //Nota: questo metodo sembra venga chiamato soltanto dopo il termine del corpo di loginButton.setOnClickListener()
    private void setNextStep(boolean signIn) {
        //...
    }

    private boolean internetIsAvaiable() {
        if(!InternetStatus.getInstance().isOnline()) {
            Toast.makeText(loginActivity.getApplicationContext(), "Connessione ad internet non disponibile!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}*/
