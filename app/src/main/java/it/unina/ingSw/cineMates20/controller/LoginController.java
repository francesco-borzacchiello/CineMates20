package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.activity.LoginActivity;
import it.unina.ingSw.cineMates20.view.activity.RegistrationActivity;
import it.unina.ingSw.cineMates20.view.activity.ResetPasswordActivity;
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

    public Runnable getEventHandlerForOnClickLogin(){
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(loginActivity))
                return;

            try {
                //Nota: in caso di wifi spento, non possiamo trovarci qui.
                Amplify.Auth.signIn(
                        loginActivity.getUsername(),
                        loginActivity.getPassword(),
                        result -> getLoginControllerInstance().setNextLoginStep(result.isSignInComplete(), loginActivity.getUsername()),
                        error -> getLoginControllerInstance().setNextLoginStep(false, loginActivity.getUsername())
                );
            } catch (Exception error) {
                Log.e("AuthQuickstart", "Could not initialize Amplify", error);
            }
        };
    }

    public Runnable getEventHandlerForOnClickRegistration(boolean isSocialLogin , String socialProvider) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(loginActivity))
                return;

            Intent myIntent = new Intent(loginActivity, RegistrationActivity.class);

            myIntent.putExtra("isSocialLogin", isSocialLogin);
            myIntent.putExtra("socialProvider", socialProvider);

            loginActivity.startActivity(myIntent);
        };
    }

    public Runnable getEventHandlerForOnClickResetPassword() {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(loginActivity))
                return;

            Intent myIntent = new Intent(loginActivity, ResetPasswordActivity.class);
            loginActivity.startActivity(myIntent);
        };
    }

    private void setNextLoginStep(boolean signIn, String username) {
        if(Utilities.checkNullActivityOrNoConnection(loginActivity))
            return;

        if(signIn) {
            //Occorre runOnUiThread, pena lancio di un'eccezione a runtime
            loginActivity.runOnUiThread(() -> Utilities.stampaToast(loginActivity, "Benvenuto " + username));

            //Mostra schermata home con un intent, passando LoginActivity come parent e poi distruggendo tutte le activity create...
            Intent intent = new Intent(loginActivity, HomeActivity.class);
            loginActivity.runOnUiThread(() -> Utilities.clearBackStack(intent));
            loginActivity.startActivity(intent);
            loginActivity.finish();
        }
        else {
            loginActivity.runOnUiThread(() -> Utilities.stampaToast(loginActivity, "Credenziali errate."));
        }
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
