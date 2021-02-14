package it.unina.ingSw.cineMates20.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.RegistrationController;
import it.unina.ingSw.cineMates20.view.fragment.ConfirmRegistrationCodeFragment;
import it.unina.ingSw.cineMates20.view.fragment.RegistrationFragment;
import it.unina.ingSw.cineMates20.view.util.Utilities;


public class RegistrationActivity extends AppCompatActivity {

    private RegistrationController registrationController;
    private boolean isSocialRegistration;
    private RegistrationFragment registrationFragment;
    private FragmentManager manager;
    private ConfirmRegistrationCodeFragment fragmentConfermaCodice;
    private String username;
    private Uri profileImageUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registrationController = RegistrationController.getRegistrationControllerInstance();
        registrationController.setRegistrationActivity(this);

        Bundle loginBundle = getIntent().getExtras();
        String socialProviderRegistration;
        if(loginBundle != null) {
            isSocialRegistration = loginBundle.getBoolean("isSocialLogin");
            socialProviderRegistration = loginBundle.getString("socialProvider");
        }
        else { //in caso di loginType null si verrà reindirizzati alla pagina precedente
            finish();
            return;
        }

        if(isSocialRegistration) {
            if(socialProviderRegistration.equals("facebook"))
                registrationController.startFacebookLogin();
            else if(socialProviderRegistration.equals("google"))
                registrationController.startGoogleLogin();
            else {
                Utilities.stampaToast(this, "Funzionalità in sviluppo!");
                finish();
            }
        }
        else { //Si mostra il fragment per la registrazione interna
            registrationFragment = new RegistrationFragment();

            registrationFragment.setRegistrationButtonRunnable(registrationController.
                    getEventHandlerForOnClickRegistration());
            registrationFragment.setAbilitaRegistrazioneTextWatcher(registrationController.
                    getAbilitaRegistrazioneTextWatcher());
            registrationFragment.setMostraPasswordCheckBoxListener(registrationController.
                    getMostraPasswordCheckBoxListener());
            registrationFragment.setListenerToLoadImage(registrationController.
                    getEventHandlerForOnClickSetProfileImage());

            createAndShowNewFragment(registrationFragment);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == registrationController.getPickImageCode() && resultCode == RESULT_OK) {
            profileImageUri = data.getData();
            registrationFragment.updateProfileImage(profileImageUri);
        } else if (requestCode == 1111) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            registrationController.handleGoogleSignInResult(task);
        }
        else if(FacebookSdk.isFacebookRequestCode(requestCode)) {
            if (!registrationController.getFacebookCallbackManager().onActivityResult(requestCode, resultCode, data))
                Utilities.stampaToast(this, "Si è verificato un errore,\nriprova più tardi.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == registrationController.getPickImageCode()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                registrationController.launchGalleryIntentPicker();
            else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Hai rifiutato la concessione di questa autorizzazione.\n" +
                        "Devi concedere il permesso in \"Permessi\" nelle impostazioni\n del tuo dispositivo, prima di poter selezionare una foto.",
                        Snackbar.LENGTH_LONG).setAction("Impostazioni", view ->
                                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + BuildConfig.APPLICATION_ID))));

                View snackbarView = snackbar.getView();
                TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setMaxLines(6);
                snackbar.setDuration(6000);
                snackbar.show();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Per il login social
    public void showHomeOrRegistrationPage(String nome, String cognome, Uri socialImageUri) {
        profileImageUri = socialImageUri;

        if(!registrationController.isUserAlreadyRegistered()) {
            registrationFragment = new RegistrationFragment(nome, cognome);

            registrationFragment.setRegistrationButtonRunnable(registrationController.
                    getEventHandlerForOnClickRegistration());
            registrationFragment.setAbilitaRegistrazioneTextWatcher(registrationController.
                    getAbilitaRegistrazioneTextWatcher());
            registrationFragment.setListenerToLoadImage(registrationController.
                    getEventHandlerForOnClickSetProfileImage());

            if(socialImageUri != null) {
                Thread t = new Thread(() -> {
                    try {
                        URL url = new URL(socialImageUri.toString());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            connection.setDoInput(true);
                            connection.connect();
                        }

                        InputStream input = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(input);

                        registrationFragment.setProfileImageBitmap(bitmap);
                    } catch (IOException ignore) {
                    }
                });
                t.start();
                try {
                    t.join();
                } catch (InterruptedException ignore) {
                }
            }
            createAndShowNewFragment(registrationFragment);
        } else { //Si deallocano le activity e si mostra la home
            Utilities.stampaToast(this, "Login effettuato con successo");
            HomeController.getHomeControllerInstance().startFromLogin(this);
        }
    }

    public boolean getLoginOrRegistrationType() {
        return isSocialRegistration;
    }

    public void createAndShowNewFragment(Fragment fragment) {
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.frameLayoutFragmentRegistrazione, fragment);
        transaction.commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }

    public void mostraFragmentConfermaCodice() {
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.anim.fragment_fade_exit);
        fragmentConfermaCodice = new ConfirmRegistrationCodeFragment();

        fragmentConfermaCodice.setTextWatcherConfermaCodice(registrationController.getConfermaCodiceTextWatcher());
        fragmentConfermaCodice.setInviaCodiceOnClickListener(registrationController.getInviaCodiceOnClickListener());
        fragmentConfermaCodice.setReinviaCodiceOnClickListener(registrationController.getReinviaCodiceOnClickListener());

        transaction.replace(R.id.frameLayoutFragmentRegistrazione, fragmentConfermaCodice);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public void setEnableSendButton(boolean enable) {
        if(fragmentConfermaCodice != null)
            fragmentConfermaCodice.setEnableSendButton(enable);
    }

    public int getLengthEditTextInviaCodice() {
        if(fragmentConfermaCodice != null)
            return fragmentConfermaCodice.getLengthEditTextInviaCodice();
        return -1;
    }

    public String getCodiceDiConferma() {
        if(fragmentConfermaCodice != null)
            return fragmentConfermaCodice.getCodiceDiConferma();
        return null;
    }

    public String getEmail() {
        if(registrationFragment != null)
            return registrationFragment.getEmail();
        return null;
    }

    public String getUsername() {
        if(registrationFragment != null)
            username = registrationFragment.getUsername();
        return username;
    }

    public String getCognome() {
        if(registrationFragment != null)
            return registrationFragment.getCognome();
        return null;
    }

    public String getNome() {
        if(registrationFragment != null)
            return registrationFragment.getNome();
        return null;
    }

    public String getPassword() {
        if(registrationFragment != null)
            return registrationFragment.getPassword();
        return null;
    }

    public String getConfermaPassword() {
        if(registrationFragment != null)
            return registrationFragment.getConfermaPassword();
        return null;
    }

    public void enableRegisterButtonIfTextIsNotEmpty() {
        if(registrationFragment != null)
            registrationFragment.enableRegisterButtonIfTextIsNotEmpty();
    }

    public boolean isMostraPasswordChecked() {
        if(registrationFragment != null)
            return registrationFragment.isMostraPasswordChecked();
        return false;
    }

    public void showOrHidePassword(boolean show) {
        if(registrationFragment != null)
            registrationFragment.showOrHidePassword(show);
    }

    public void updatePasswordFocus() {
        if(registrationFragment != null)
            registrationFragment.updatePasswordFocus();
    }

    public void returnToLogin() {
        if(fragmentConfermaCodice != null)
            manager.popBackStack();
        if(registrationFragment != null)
            manager.popBackStack();
        finish();
    }
}
