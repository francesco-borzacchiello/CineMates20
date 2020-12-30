package it.unina.ingSw.cineMates20.controller;

import android.util.Log;

import com.amplifyframework.core.Amplify;

public class MainController {

    ControllerLogin controllerLogin;

    public MainController() {
        this.controllerLogin = new ControllerLogin();
    }

    public void start(){
        Amplify.Auth.signOut(
                () -> Log.i("AuthQuickstart", "Signed out successfully"),
                error -> Log.e("AuthQuickstart", error.toString())
        );
        checkIfUserIsAlreadyLoggedIn();
    }

    private void checkIfUserIsAlreadyLoggedIn() {
        Amplify.Auth.fetchAuthSession(
                isSignIn -> ifIsLoggedInOpenHomeElseOpenLogin(isSignIn.isSignedIn()),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }

    private void ifIsLoggedInOpenHomeElseOpenLogin(boolean isSignedIn) {
        if (isSignedIn)
            openHomeActivity();
        else
            login();
    }

    private void login() {
        controllerLogin.Start();
    }

    private void openHomeActivity() {
        //TODO: Gestione dell'apertura dell'activity per la home (con relativo controller)
        Log.i("Home", "l'utente è già loggato");
    }
}
