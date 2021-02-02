package it.unina.ingSw.cineMates20.controller;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;
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

import java.util.ArrayList;
import java.util.Arrays;

import it.unina.ingSw.cineMates20.model.S3Manager;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.model.UserHttpRequests;
import it.unina.ingSw.cineMates20.view.activity.RegistrationActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

import static com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.SIGN_IN_CANCELLED;

public class RegistrationController {
    private static RegistrationController instance;
    private RegistrationActivity registrationActivity;
    private CallbackManager facebookCallbackManager;
    private Uri socialImageUri;

    private final int PICK_IMAGE = 1;

    private RegistrationController() {}

    public static RegistrationController getRegistrationControllerInstance() {
        if(instance == null)
            instance = new RegistrationController();
        return instance;
    }

    public void setRegistrationActivity(RegistrationActivity activity) { this.registrationActivity = activity; }

    public View.OnClickListener getEventHandlerForOnClickSetProfileImage() {
        return v -> ActivityCompat.requestPermissions
                       (registrationActivity, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, PICK_IMAGE);
    }

    public void launchGalleryIntentPicker() {
        if(registrationActivity == null) return;

        Intent gallery = new Intent();
        gallery.setType("image/*");

        gallery.setAction(Intent.ACTION_GET_CONTENT);
        registrationActivity.startActivityForResult
                (Intent.createChooser(gallery, "Seleziona l'immagine del profilo"), PICK_IMAGE);
    }

    public int getPickImageCode() {
        return PICK_IMAGE;
    }

    public Runnable getEventHandlerForOnClickRegistration() {
        if(registrationActivity == null)
            return null;

        return ()-> {
            boolean isSocialRegistration = registrationActivity.getLoginOrRegistrationType();
            Context context = registrationActivity.getApplicationContext();
            if(context != null && !Utilities.isOnline()) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Connessione ad internet non disponibile!"));
                return;
            }

            boolean inputIsValid = isInputValid(isSocialRegistration);

            if(!isSocialRegistration && inputIsValid) {
                //Si procede con la registrazione interna
                ArrayList<AuthUserAttribute> attributes = new ArrayList<>();
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.email(), registrationActivity.getEmail()));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.preferredUsername(), registrationActivity.getUsername()));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.familyName(), registrationActivity.getCognome()));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.givenName(), registrationActivity.getNome()));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.name(), ""));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.picture(), "null"));

                Amplify.Auth.signUp(
                        registrationActivity.getUsername(),
                        registrationActivity.getPassword(),
                        AuthSignUpOptions.builder().userAttributes(attributes).build(),
                        result -> Log.i("signUp", "Result: " + result.toString()),
                        error -> Amplify.Auth.resendSignUpCode
                                (
                                  registrationActivity.getUsername(),
                                  result2 -> Log.i("signUp", "Result: " + result2.toString()),
                                  error2 -> Log.e("signUp", "Result: " + error2.toString())
                                )
                );

                registrationActivity.mostraFragmentConfermaCodice(); //TODO: questo va fatto solo se result è corretto, gestire caso dati già esistenti ma non confermati
            }
            else if(inputIsValid) { //Si procede con la registrazione social
                String email = UserHttpRequests.getInstance().getSocialUserEmail(registrationActivity);

                if(email != null) {
                    if (insertNewUser(new UserDB(registrationActivity.getUsername(), registrationActivity.getNome(), registrationActivity.getCognome(), email, "utente"))) {
                        S3Manager.uploadImage(registrationActivity, registrationActivity.getProfileImageUri(), email);
                        socialImageUri = null; //Reset uri foto profilo

                        registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Benvenuto " + registrationActivity.getNome()));
                        HomeController.getHomeControllerInstance().startFromLogin(registrationActivity);
                    }
                }
                else
                    registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Si è verificato un errore"));
            }
        };
    }

    private boolean insertNewUser(UserDB user) {
        return UserHttpRequests.getInstance().createNewUser(user);
    }

    public boolean isUserAlreadyRegistered() {
        return UserHttpRequests.getInstance().getSocialLoggedUser(registrationActivity) != null;
    }

    private boolean isInputValid(boolean isSocialRegistration) {
        /* Nome e cognome sono per forza validi in quanto è soltanto richiesto che siano non vuoti,
         * e il tasto registrati viene disabilitato non appena una EditText diventa vuota. */

        if (!Utilities.isUserNameValid(registrationActivity.getUsername())) {
            registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "L'username inserito non è valido oppure è già in uso."));
            return false;
        }

        if(!isSocialRegistration) {
            if (!Utilities.isEmailValid(registrationActivity.getEmail())) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "L'email inserita non è valida oppure è già in uso."));
                return false;
            }
            if (!Utilities.isPasswordValid(registrationActivity.getPassword())) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "La password deve contenere almeno un numero, " +
                        "un carattere speciale, una lettera minuscola e una maiuscola."));
                return false;
            }
            if (!Utilities.isConfirmPasswordValid(registrationActivity.getPassword(), registrationActivity.getConfermaPassword())) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Le password non coincidono!"));
                return false;
            }
        }

        return true;
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
                    //Se tutte le EditText sono non vuote, abilita il tasto Registrati
                    registrationActivity.enableRegisterButtonIfTextIsNotEmpty();
                }
            }
        };
    }

    public View.OnClickListener getMostraPasswordCheckBoxListener() {
        return listener -> {
            registrationActivity.showOrHidePassword(registrationActivity.isMostraPasswordChecked());
            registrationActivity.updatePasswordFocus();
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

                                String first_name = object.getString("first_name");
                                String last_name = object.getString("last_name");

                                //Recupero uri foto profilo
                                String id = object.getString("id");
                                socialImageUri = Uri.parse("https://graph.facebook.com/" + id + "/picture?type=large");

                                registrationActivity.showHomeOrRegistrationPage(first_name, last_name, socialImageUri);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Login annullato"));
                registrationActivity.finish();
            }

            @Override
            public void onError(FacebookException exception) {
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Si è verificato un errore"));
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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder
                (GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(registrationActivity, gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        registrationActivity.startActivityForResult(signInIntent, 1111);
    }

    public void handleGoogleSignInResult(@NotNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if(account != null) {
                socialImageUri = account.getPhotoUrl();
                registrationActivity.showHomeOrRegistrationPage(account.getGivenName(), account.getFamilyName(), socialImageUri);
            }
            else
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Si è verificato un errore"));
        } catch (ApiException e) {
            if(e.getStatusCode() == SIGN_IN_CANCELLED)
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Login annullato"));
            else
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Si è verificato un errore"));

            registrationActivity.finish();
        }
    }

    public TextWatcher getConfermaCodiceTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(registrationActivity == null)
                    return;

                registrationActivity.setEnableSendButton(registrationActivity.getLengthEditTextInviaCodice() > 0);
            }
        };
    }

    public View.OnClickListener getInviaCodiceOnClickListener() {
        return listener -> {
            //Codice per confermare l'email
            Amplify.Auth.confirmSignUp(
                    registrationActivity.getUsername(),
                    registrationActivity.getCodiceDiConferma(),
                    result -> handleSendedVerificationCode(result.isSignUpComplete(), registrationActivity.getEmail()),
                    error -> handleSendedVerificationCode(false, null)
            );
        };
    }

    private void handleSendedVerificationCode(boolean isSignUpComplete, String email) {
        if(isSignUpComplete) {
            //Reindirizzare a pagina login
            if(insertNewUser(new UserDB(registrationActivity.getUsername(), registrationActivity.getNome(),
                    registrationActivity.getCognome(), email, "utente"))) {

                if(registrationActivity.getProfileImageUri() != null) //Allora l'utente ha modificato la foto di default
                    S3Manager.uploadImage(registrationActivity, registrationActivity.getProfileImageUri(),
                                          registrationActivity.getEmail());

                registrationActivity.returnToLogin();
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Account creato con successo"));
                socialImageUri = null; //Reset uri foto profilo in caso di tentativo social precedente
            }
            else
                registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Si è verificato un errore.\nRiprova tra qualche minuto."));
        }
        else
            registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Codice errato"));
    }

    public View.OnClickListener getReinviaCodiceOnClickListener() {
        return listener -> Amplify.Auth.resendSignUpCode(
                registrationActivity.getUsername(),
                result -> registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "Codice reinviato")),
                error -> registrationActivity.runOnUiThread(() -> Utilities.stampaToast(registrationActivity, "L'email inserita non è valida oppure è già in uso."))
        );
    }
}
