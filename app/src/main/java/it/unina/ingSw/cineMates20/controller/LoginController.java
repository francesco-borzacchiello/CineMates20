package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

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
    private RegistrationActivity registrationActivity;

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

    public void setRegistrationActivity(RegistrationActivity activity) {this.registrationActivity = activity; }

    public RegistrationActivity getRegistrationActivity() { return registrationActivity; }


    public Runnable eventHandlerForOnClickLogin(String username, String password){
        return () -> {
            if(checkNullActivityOrNoConnection(loginActivity))
                return;

            try {
                //Nota: in caso di wifi spento, non possiamo trovarci qui.
                Amplify.Auth.signIn(
                        "carmineG",    //TODO: sostituire con "username"
                        "Carmine_97",  //TODO: sostituire con "password"
                        //result -> Log.i("login", "risultato del login:" + result.isSignInComplete()),
                        result -> getLoginControllerInstance().setNextLoginStep(result.isSignInComplete(), username),
                        error -> Log.e("login", error.toString())
                );
            } catch (Exception error) {
                Log.e("AuthQuickstart", "Could not initialize Amplify", error);
            }
        };
    }

    public Runnable eventHandlerForOnClickRegistration(boolean isSocialLogin , String socialProvider) {
        return () -> {
            if(checkNullActivityOrNoConnection(loginActivity))
                return;

            Intent myIntent = new Intent(loginActivity, RegistrationActivity.class);

            myIntent.putExtra("isSocialLogin", isSocialLogin);
            myIntent.putExtra("socialProvider", socialProvider);

            loginActivity.startActivity(myIntent);

            //TODO: moriva perchè non potevamo recuperare l'activity
            /*Intent myIntent = new Intent(activity, RegistrationActivity.class);
            myIntent.putExtra("loginType", false);
            activity.startActivity(myIntent);*/

            //Non lanciare finish() in quanto in caso di pressione di tasto indietro si verrà reindirizzati a LoginActivity
        };
    }


    //TODO: Se il login è fallito allora mostra errori a schermo, altrimenti passa alla prossima activity, chiama Utilities.clearBackStack(), setta loginActivity=null e infine finish()
    public void setNextLoginStep(boolean signIn, String username) {
        if(checkNullActivityOrNoConnection(loginActivity))
            return;

        if(signIn) {
            //Occorre runOnUiThread, pena lancio di un'eccezione a runtime
            loginActivity.runOnUiThread(() -> Utilities.stampaToast(loginActivity, "Benvenuto " + username));

            //Mostra schermata home con un intent, passando LoginActivity come parent e poi distruggendo tutte le activity create...
            Intent intent = new Intent(); //TODO: sostituire con new Intent(loginActivity, HomeActivity.class);
            Utilities.clearBackStack(intent); //Nota: testato già con TmpActivity, elimina solo dopo aver chiamato startActivity()
            //loginActivity.startActivity(intent);
        }
        else {
            Utilities.stampaToast(loginActivity, "Credenziali errate.");
        }
    }

    private boolean checkNullActivityOrNoConnection(Activity activity) {
        if(activity == null)
            //TODO: gestire questo caso (non si può chiamare stampaToast poiché activity è null)
            //....
            return true; //null activity

        if(!Utilities.isOnline(getLoginActivity())) {
            Utilities.stampaToast(activity, activity.getApplicationContext().getResources().getString(R.string.networkNotAvailable));
            return true; //no connection
        }

        return false;
    }


    public TextWatcher usernameLoginTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(loginActivity == null)
                    return;

                Button loginButton = loginActivity.findViewById(R.id.loginButton);
                EditText username = loginActivity.findViewById(R.id.usernameLogin);
                EditText password = loginActivity.findViewById(R.id.passwordLogin);

                if(!Utilities.isEmailValid(username.getText().toString()) && !Utilities.isUserNameValid(username.getText().toString())) {
                    username.setError("Email o username non valido");
                    loginButton.setEnabled(false);
                }
                else if(Utilities.isPasswordValid(password.getText().toString()))
                    loginButton.setEnabled(true);
            }
        };
    }

    public TextWatcher passwordLoginTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(loginActivity == null)
                    return;

                Button loginButton = loginActivity.findViewById(R.id.loginButton);
                EditText username = loginActivity.findViewById(R.id.usernameLogin);
                EditText password = loginActivity.findViewById(R.id.passwordLogin);

                if(!Utilities.isPasswordValid(password.getText().toString())) {
                    password.setError("La password non è valida");
                    loginButton.setEnabled(false);
                }
                else if(Utilities.isEmailValid(username.getText().toString()) || Utilities.isUserNameValid(username.getText().toString()))
                    loginButton.setEnabled(true);
            }
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
