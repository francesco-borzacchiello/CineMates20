package it.unina.ingSw.cineMates20.controller;

import android.util.Log;

import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.EntryPoint;

/**
 * Verifica se l'utente è loggato comunicando con cognito.
 * Se loggato aprirà la home, altrimenti la pagina di login
 */
public class MainController {

    LoginController controllerLogin;
    EntryPoint activity;

    public MainController(EntryPoint entryPoint) {
        this.activity = entryPoint;
        this.controllerLogin = LoginController.getLoginControllerInstance();
    }

    public void start(){
        //Lancia eccezione, non va bene
        /*Amplify.Auth.fetchAuthSession(
            isSignIn -> ifIsLoggedInOpenHomeElseOpenLogin(isSignIn.isSignedIn()),
            error -> Log.e("AuthQuickstart", error.toString())
        );*/

        AuthUser user = Amplify.Auth.getCurrentUser(); //Se l'utente non è autenticato, restituisce null
        activity.overridePendingTransition(0, 0);

        //TODO: sostituire opportunamente == e != alla fine del test sul login
        if(user == null) openHomeActivity();
        else openLoginActivity();

        activity.finish();
    }

    private void openLoginActivity() { controllerLogin.start(activity); }

    private void openHomeActivity() {
        //TODO: Gestione dell'apertura dell'activity per la home (con relativo controller)
        Log.i("Home", "l'utente è già loggato");
    }
}
