package it.unina.ingSw.cineMates20.controller;

import android.util.Log;

import com.amplifyframework.core.Amplify;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

/**
 * Verifica se l'utente è loggato comunicando con cognito,
 * se non è loggato con cognito, verifica se è loggato con google o facebook.
 * Se loggato aprirà la home, altrimenti la pagina di login
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

        //TODO: modificare opportunamente isLoggedIn() togliendo il ! alla fine del test sul login
        if(isLoggedIn()) openHomeActivity();
        else openLoginActivity();

        activity.finish();
    }
    //endregion

    //region Verifica se un utente è loggato si apre la home, altrimenti il login
    private boolean isLoggedIn() {
        /*AuthUser user = Amplify.Auth.getCurrentUser(); //Se l'utente non è autenticato, restituisce null
        if(user != null) return true;

        //Verifica se l'utente è loggato con Facebook
        //Nota: il token di Facebook scade circa 60 giorni dall'ultimo utilizzo dell'applicazione
        AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        if(fbAccessToken != null)
            return true;

        // Se l'utente è già loggato, restituisce l'account con il quale si è loggato l'ultima volta
        return GoogleSignIn.getLastSignedInAccount(activity) != null;*/

        /*
        * Verifica se un utente è loggato internamente tramite amplify,
        * oppure se un utente è loggato tramite facebook,
        * oppure se un utente è loggato tramite google
        * */
        return  Amplify.Auth.getCurrentUser() != null ||
                ((AccessToken.getCurrentAccessToken() != null ||
                  GoogleSignIn.getLastSignedInAccount(activity) != null)
                  && isUserAlreadyRegistered());

    }

    public boolean isUserAlreadyRegistered() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String url = activity.getResources().getString(R.string.db_path) + "User/getById/{email}";
        String email;

        if(AccessToken.getCurrentAccessToken() != null)
            email = Utilities.tryToGetFacebookEmail();
        else
            email = Utilities.tryToGetGoogleEmail(activity);

        if(email == null) return false;

        try {
            UserDB userDB = restTemplate.getForObject(url, UserDB.class, email);
            if(userDB != null)
                return true;
        }catch(HttpClientErrorException ignore){}

        return false;
    }

    private void openLoginActivity() { controllerLogin.start(activity); }

    private void openHomeActivity() {
        controllerHome.start(activity);
        Log.i("Home", "l'utente è già loggato");
    }
    //endregion
}
