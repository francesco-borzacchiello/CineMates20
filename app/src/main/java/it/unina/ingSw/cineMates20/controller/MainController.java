package it.unina.ingSw.cineMates20.controller;

import android.util.Log;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.EntryPoint;

public class MainController {
    LoginController controllerLogin;
    EntryPoint activity;

    public MainController(EntryPoint entryPoint) {
        this.activity = entryPoint;
        this.controllerLogin = new LoginController();
    }

    public void start(){
        checkIfUserIsAlreadyLoggedIn();
    }

    private void checkIfUserIsAlreadyLoggedIn() {
        Amplify.Auth.fetchAuthSession(
                isSignIn -> ifIsLoggedInOpenHomeElseOpenLogin(isSignIn.isSignedIn()),
                //TODO: in secondo momento controllare cosa mettere sugli error
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }

    private void ifIsLoggedInOpenHomeElseOpenLogin(boolean isSignedIn) {
        //TODO: togliere ! alla fine del test sul login
        if (!isSignedIn) openHomeActivity();
        else openLoginActivity();
    }

    private void openLoginActivity() {
        controllerLogin.start(activity);
    }

    private void openHomeActivity() {
        //TODO: Gestione dell'apertura dell'activity per la home (con relativo controller)
        Log.i("Home", "l'utente è già loggato");
    }
}
