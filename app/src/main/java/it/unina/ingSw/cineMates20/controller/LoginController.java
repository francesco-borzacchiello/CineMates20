package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.LoginActivity;
import it.unina.ingSw.cineMates20.view.activity.RegistrationActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

/**
 * Effettua il login comunicando con cognito, se esso ha successo si viene reindirizzati alla home
 */
public class LoginController {

    private static LoginController instance;
    private LoginActivity loginActivity;

    private LoginController() {}

    public static LoginController getLoginControllerInstance() {
        if(instance == null)
            instance = new LoginController();
        return instance;
    }

    //activityParent è un riferimento a "EntryPoint"
    public void start(Activity activityParent){
        //Impostiamo come parent dell'activity il riferimento ad EntryPoint, che verrà poi terminata dal MainController
        Intent intent = new Intent(activityParent, LoginActivity.class);
        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(0,0);
    }

    public void setLoginActivity(LoginActivity activity) {
        this.loginActivity = activity;
    }

    public LoginActivity getLoginActivity() { return loginActivity; }

    public Runnable getEventHandlerForOnClickLogin(String username, String password){
        return () -> {
            if(checkNullActivityOrNoConnection(loginActivity))
                return;

            try {
                //Nota: in caso di wifi spento, non possiamo trovarci qui.
                //Nota 2: se l'utente già loggato prova a rifare login con credenziali errate, Amplify dirà login con successo
                Amplify.Auth.signIn(
                        loginActivity.getUsername(),
                        loginActivity.getPassword(),
                        //result -> Log.i("login", "risultato del login:" + result.isSignInComplete()),
                        result -> getLoginControllerInstance().setNextLoginStep(result.isSignInComplete(), username),
                        error -> getLoginControllerInstance().setNextLoginStep(false, username)
                        //error -> Log.e("loginAmplify", error.toString())
                );
            } catch (Exception error) {
                Log.e("AuthQuickstart", "Could not initialize Amplify", error);
            }
        };
    }

    public Runnable getEventHandlerForOnClickRegistration(boolean isSocialLogin , String socialProvider) {
        return () -> {
            if(checkNullActivityOrNoConnection(loginActivity))
                return;

            Intent myIntent = new Intent(loginActivity, RegistrationActivity.class);

            myIntent.putExtra("isSocialLogin", isSocialLogin);
            myIntent.putExtra("socialProvider", socialProvider);

            loginActivity.startActivity(myIntent);
        };
    }

    public void setNextLoginStep(boolean signIn, String username) {
        if(checkNullActivityOrNoConnection(loginActivity))
            return;

        if(signIn) {
            //Occorre runOnUiThread, pena lancio di un'eccezione a runtime
            loginActivity.runOnUiThread(() -> Utilities.stampaToast(loginActivity, "Benvenuto " + username));

            //Mostra schermata home con un intent, passando LoginActivity come parent e poi distruggendo tutte le activity create...
            Intent intent = new Intent(); //TODO: sostituire con new Intent(loginActivity, HomeActivity.class);
            loginActivity.runOnUiThread(() -> Utilities.clearBackStack(intent)); //Nota: testato già con TmpActivity, elimina solo dopo aver chiamato startActivity()
            //loginActivity.startActivity(intent);
            //loginActivity.finish();
        }
        else {
            loginActivity.runOnUiThread(() -> Utilities.stampaToast(loginActivity, "Credenziali errate."));
        }
    }

    private boolean checkNullActivityOrNoConnection(Activity activity) {
        if(activity == null)
            //TODO: gestire questo caso (non si può chiamare stampaToast poiché activity è null)
            //....
            return true; //null activity

        if(!Utilities.isOnline(getLoginActivity())) {
            loginActivity.runOnUiThread(() -> Utilities.stampaToast(activity, activity.getApplicationContext().getResources().getString(R.string.networkNotAvailable)));
            return true; //no connection
        }

        return false;
    }


    public TextWatcher getUsernameLoginTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(loginActivity == null)
                    return;

                if(!Utilities.isEmailValid(loginActivity.getUsername()) && !Utilities.isUserNameValid(loginActivity.getUsername())) {
                    loginActivity.showUsernameError();
                    loginActivity.enableLoginButton(false);
                }
                else if(Utilities.isPasswordValid(loginActivity.getPassword()))
                    loginActivity.enableLoginButton(true);
            }
        };
    }

    public TextWatcher getPasswordLoginTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(loginActivity == null)
                    return;

                if(!Utilities.isPasswordValid(loginActivity.getPassword())) {
                    loginActivity.showPasswordError();
                    loginActivity.enableLoginButton(false);
                }
                else if(Utilities.isEmailValid(loginActivity.getUsername()) || Utilities.isUserNameValid(loginActivity.getUsername()))
                    loginActivity.enableLoginButton(true);
            }
        };
    }

    public View.OnClickListener getMostraPasswordCheckBoxListener() {
        return listener -> {
            //Se la CheckBox è selezionata
            if (loginActivity.isCheckBoxMostraPasswordEnabled()) {
                // mostra password
                loginActivity.showOrHidePassword(true);
            } else {
                // nascondi password
                loginActivity.showOrHidePassword(false);
            }

            loginActivity.updatePasswordFocus();
        };
    }
}

/* VECCHIO CODICE TEMPORANEO
public class LoginController {

    Activity activityParent;
    public LoginController() {}

    public void start(Activity activityParent) { startLoginActivity(activityParent); }

    private void startLoginActivity(Activity activityParent){
        //Runnable per il listener sulla pressione del pulsante Login
        Intent intent;
        intent = new Intent(activityParent, LoginActivity.class);
        initializesEventHandlersForTheLoginActivity(intent);
        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(0,0);
    }

    //Nota: questo metodo sembra venga chiamato soltanto dopo il termine del corpo di loginButton.setOnClickListener()
    public void setNextStep(boolean signIn) {
        //...
    }

    private void initializesEventHandlersForTheLoginActivity(Intent intent){
        Runnable eventHandlerForOnClickLogin = eventHandlerForOnClickLogin(),
                 eventHandlerForOnClickRegistration = eventHandlerForOnClickRegistration();

        intent.putExtra(IdentifiedForEventHandlers.ON_CLICK_LOGIN, new Utilities.Srunnable(eventHandlerForOnClickLogin));
        intent.putExtra(IdentifiedForEventHandlers.ON_CLICK_REGISTRATION, new Utilities.Srunnable(eventHandlerForOnClickRegistration));
    }

    private Runnable eventHandlerForOnClickLogin(){
        return (Runnable & Serializable)() -> {
            try {
                //TODO: Aggiungere logica login in setNextStep(), se il login va a buon fine (result.isSignInComplete() == true), reindirizzare alla Home, altrimenti mostrare errore a schermo
                //Nota: in caso di wifi spento, non possiamo trovarci qui.
                Amplify.Auth.signIn(
                        "carmineG", //TODO: sostituire con credenziali date in input
                        "Carmine_97",
                        result -> Log.i("login", "risultato del login:" + result.isSignInComplete()),  //setNextStep(result.isSignInComplete())
                        error -> Log.e("login", error.toString())
                );
            } catch (Exception error) {
                Log.e("AuthQuickstart", "Could not initialize Amplify", error);
            }
        };
    }

    private Runnable eventHandlerForOnClickRegistration() {
        return (Runnable & Serializable)() -> {
            //TODO: muore perchè non possiamo recuperare l'activity
            /*Intent myIntent = new Intent(activity, RegistrationActivity.class);
            myIntent.putExtra("loginType", false);
            activity.startActivity(myIntent);
            //Non lanciare finish() in quanto in caso di pressione di tasto indietro si verrà reindirizzati qui
        };
    }
}*/
