package it.unina.ingSw.cineMates20.controller;

import android.util.Log;

import com.amplifyframework.core.Amplify;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserHttpRequests;

/**
 * Verifica se l'utente è loggato comunicando con Cognito,
 * se non è loggato con cognito, verifica se è loggato con google o facebook.
 * Se loggato aprirà la home, altrimenti la pagina di login.
 */
public class MainController {

    //region Attributi
    private final LoginController controllerLogin;
    private final EntryPoint activity;
    private final HomeController controllerHome;
    //endregion

    //region Costruttore
    public MainController(EntryPoint entryPoint) {
        this.activity = entryPoint;
        this.controllerLogin = LoginController.getLoginControllerInstance();
        this.controllerHome = HomeController.getHomeControllerInstance();
    }
    //endregion

    //region Punto di avvio del controller
    public void start(){
        activity.overridePendingTransition(0, 0);

        //Inizializzazione SettingsController
        SettingsController.setSettingsControllerContextActivity(activity);
        SettingsController.getSettingsControllerInstance();

        if(isLoggedIn()) openHomeActivity();
        else openLoginActivity();

        activity.finish();
    }
    //endregion

    //region Verifica se un utente è loggato si apre la home, altrimenti il login
    private boolean isLoggedIn() {
       /* Verifica se un utente è loggato internamente tramite amplify,
        * oppure se un utente è loggato tramite facebook,
        * oppure se un utente è loggato tramite google
        */
        return  Amplify.Auth.getCurrentUser() != null ||
                ((AccessToken.getCurrentAccessToken() != null ||
                  GoogleSignIn.getLastSignedInAccount(activity) != null)
                  && isUserAlreadyRegistered());

    }

    private boolean isUserAlreadyRegistered() {
        return UserHttpRequests.getInstance().
                isUserAlreadyRegistered(User.getLoggedUser(activity).getEmail());
    }

    private void openLoginActivity() { controllerLogin.start(activity); }

    private void openHomeActivity() {
        controllerHome.start(activity);
        Log.i("Home", "l'utente è già loggato");
    }
    //endregion
}
