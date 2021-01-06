package it.unina.ingSw.cineMates20.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.RegistrationController;
import it.unina.ingSw.cineMates20.view.fragment.ConfirmRegistrationCodeFragment;
import it.unina.ingSw.cineMates20.view.fragment.RegistrationFragment;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class RegistrationActivity extends AppCompatActivity {

    private RegistrationController registrationController;
    private boolean isSocialRegistration;
    private RegistrationFragment registrationFragment;
    private FragmentManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView
        setContentView(R.layout.activity_registration);

        registrationController = RegistrationController.getLoginControllerInstance();
        registrationController.setRegistrationActivity(this);

        String socialProviderRegistration;
        Bundle loginBundle = getIntent().getExtras();
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
            createAndShowNewFragment(registrationFragment);

            registrationFragment.setRegistrationButtonRunnable(registrationController.
                    getEventHandlerForOnClickRegistration());
            registrationFragment.setAbilitaRegistrazioneTextWatcher(registrationController.
                    getAbilitaRegistrazioneTextWatcher());
            registrationFragment.setMostraPasswordCheckBoxListener(registrationController.getMostraPasswordCheckBoxListener());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 10) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            registrationController.handleGoogleSignInResult(task);
        }
        else { //Se non è google, allora dev'essere sicuramente facebook
            registrationController.getFacebookCallbackManager().onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showHomeOrRegistrationPage(String nome, String cognome) {
        //if(!user.isAlreadyRegistered()) { //Comunicazione con il DAO sul server spring
            registrationFragment = new RegistrationFragment(nome, cognome);
            createAndShowNewFragment(registrationFragment);

            registrationFragment.setRegistrationButtonRunnable(registrationController.
                    getEventHandlerForOnClickRegistration());
            registrationFragment.setAbilitaRegistrazioneTextWatcher(registrationController.
                    getAbilitaRegistrazioneTextWatcher());
        //else { //Si deallocano le activity e si mostra la home tramite un metodo di registrationController
            //Utilities.stampaToast(this, "Login effettuato con successo");
            //...
        //}
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

    public boolean allEditTextAreNotEmpty() {
        return registrationFragment.allEditTextAreNotEmpty();
    }

    public CheckBox getMostraPasswordCheckBox() {
        return registrationFragment.getMostraPasswordCheckBox();
    }

    public EditText getPasswordEditText() {
        return registrationFragment.getPasswordEditText();
    }

    public EditText getConfermaPasswordEditText() {
        return registrationFragment.getConfermaPasswordEditText();
    }

    public Button getRegistrationButton() {
        return registrationFragment.getRegistrationButton();
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
        transaction.replace(R.id.frameLayoutFragmentRegistrazione, new ConfirmRegistrationCodeFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
