package it.unina.ingSw.cineMates20.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.Arrays;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.RegistrationActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class RegistrationController {
    private static RegistrationController instance;
    private RegistrationActivity registrationActivity;
    private CallbackManager facebookCallbackManager;
    private RegistrationController() {}

    public static RegistrationController getLoginControllerInstance() {
        if(instance == null)
            instance = new RegistrationController();
        return instance;
    }

    public void setRegistrationActivity(RegistrationActivity activity) { this.registrationActivity = activity; }

    public RegistrationActivity getRegistrationActivity() { return registrationActivity; }

    public Runnable getEventHandlerForOnClickRegistration() {
        if(registrationActivity == null)
            return null;

        return ()-> {
            boolean isSocialRegistration = registrationActivity.getLoginOrRegistrationType();
            Context context = registrationActivity.getApplicationContext();
            if(context != null && !Utilities.isOnline(context)) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Connessione ad internet non disponibile!"));
                return;
            }

            /* Nome e cognome sono per forza validi in quanto è soltanto richiesto che siano non vuoti,
             * e il tasto registrati viene disabilitato non appena una EditText diventa vuota. */
            EditText usernameEditText = registrationActivity.findViewById(R.id.usernameRegistrazione);
            EditText emailEditText = registrationActivity.findViewById(R.id.emailRegistrazione);
            EditText passwordEditText = registrationActivity.findViewById(R.id.passwordRegistrazione);
            EditText confermaPasswordEditText = registrationActivity.findViewById(R.id.confermaPasswordRegistrazione);

            if (!Utilities.isUserNameValid(usernameEditText.getText().toString())) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "L'username inserito non è valido oppure è già in uso."));
                return;
            }

            if(!isSocialRegistration) {
                if (!Utilities.isEmailValid(emailEditText.getText().toString())) {
                    Utilities.stampaToast(registrationActivity, "L'email inserita non è valida oppure è già in uso.");
                    return;
                }
                if (!Utilities.isPasswordValid(passwordEditText.getText().toString())) {
                    Utilities.stampaToast(registrationActivity, "La password deve contenere almeno un numero, un carattere speciale, una lettera minuscola e una maiuscola.");
                    return;
                }
                if (!Utilities.isConfirmPasswordValid(passwordEditText.getText().toString(), confermaPasswordEditText.getText().toString())) {
                    Utilities.stampaToast(registrationActivity, "Le password non coincidono!");
                    return;
                }

                /*TODO: Se non è stata modificata foto, passare url foto default a Cognito.
                 *      Procedere con la registrazione (dire a RegistrationActivity di mostrare
                 *      ConfirmRegistrationCodeFragment)*/
                //Si procede con la registrazione interna
                //...
                registrationActivity.mostraFragmentConfermaCodice();
            }
            else { //Si procede con la registrazione social
                //...
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Funzionalità in sviluppo!"));
            }
        };
    }

    public TextWatcher getAbilitaRegistrazioneTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(registrationActivity != null) {
                    Button registrationButton = registrationActivity.getRegistrationButton();
                    //Se tutte le EditText sono non vuote, abilita il tasto Registrati
                    registrationButton.setEnabled(registrationActivity.allEditTextAreNotEmpty());
                }
            }
        };
    }

    public View.OnClickListener getMostraPasswordCheckBoxListener() {
        return listener -> {
            CheckBox mostraPassword = registrationActivity.getMostraPasswordCheckBox();
            EditText passwordEditText = registrationActivity.getPasswordEditText(),
                     confermaPasswordEditText = registrationActivity.getConfermaPasswordEditText();

            //Se la CheckBox è selezionata
            if (mostraPassword.isChecked()) {
                // mostra password
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confermaPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // nascondi password
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confermaPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            if(passwordEditText.hasFocus())
                passwordEditText.setSelection(passwordEditText.length());
            else if(confermaPasswordEditText.hasFocus())
                confermaPasswordEditText.setSelection(confermaPasswordEditText.length());
        };
    }

    public FacebookCallback<LoginResult> getFacebookCallback(){
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        (object, response) -> {
                            try {
                                /*Log.d("TESTLOGFB", "fb json object: " + object);
                                  Log.d("TESTLOGFB", "fb graph response: " + response);*/

                                String id = object.getString("id");
                                String first_name = object.getString("first_name");
                                String last_name = object.getString("last_name");
                                String image_url = "http://graph.facebook.com/" + id + "/picture?type=large";

                                String email;
                                if (object.has("email")) {
                                    email = object.getString("email");
                                    Log.i("TESTLOGFB", email);
                                }
                                else email = "facebookUser@mail.com";

                                //TODO: inviare informazioni user model dell'email e memorizzare image_url per S3
                                registrationActivity.showHomeOrRegistrationPage(first_name, last_name);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,picture.type(large)"); // id,first_name,last_name,email,gender,birthday,cover,picture.type(large)
                request.setParameters(parameters);
                request.executeAndWait();
            }

            @Override
            public void onCancel() {
                Utilities.stampaToast(registrationActivity, "Login annullato");
                registrationActivity.finish();
            }

            @Override
            public void onError(FacebookException exception) {
                Utilities.stampaToast(registrationActivity, "Si è verificato un errore");
                registrationActivity.finish();
            }
        };
    }

    public CallbackManager getFacebookCallbackManager() {
        return facebookCallbackManager;
    }

    public void startFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager facebookLoginManager = LoginManager.getInstance();
        facebookLoginManager.registerCallback(facebookCallbackManager, getFacebookCallback());
        facebookLoginManager.logInWithReadPermissions(registrationActivity, Arrays.asList("public_profile", "email"));
    }

    public void startGoogleLogin() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(registrationActivity, gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        registrationActivity.startActivityForResult(signInIntent, 10); //Va bene qualunque numero coerente in onActivityResult()
    }

    public void handleGoogleSignInResult(@NotNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Si può estrarre anche l'email dell'account con account.getEmail();

            // Loggato con successo: nota esiste metodo getPhotoUrl()
            registrationActivity.showHomeOrRegistrationPage(account.getGivenName(), account.getFamilyName());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GoogleLogin", "signInResult:failed code=" + e.getStatusCode());
            Utilities.stampaToast(registrationActivity, "Si è verificato un errore:\n" + e.getStatusCode());
        }
    }
}
