package it.unina.ingSw.cineMates20.view.login.activity;

import it.unina.ingSw.cineMates20.view.login.fragment.RegistrationFragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import it.unina.ingSw.cineMates20.R;

public class RegistrationActivity extends AppCompatActivity {

    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView
        setContentView(R.layout.activity_registration);

        boolean isLoginRegistration = false;
        Bundle loginBundle = getIntent().getExtras();
        if(loginBundle != null) {
            isLoginRegistration = loginBundle.getBoolean("loginType");
        }
        else //in caso di loginType null si verrà reindirizzati alla pagina precedente
            finish();

        if(isLoginRegistration) {
            //TODO: aggiungere reindirizzamento pagina social, estrarre tutti i dati, inserirli, ritornare alla app
            //Si mostra il fragment per la registrazione social con i campi "Nome" e "Cognome" già inizializzati
            //....
            //createNewFragment(new SocialRegistrationFragment());
        }
        else { //Si mostra il fragment per la registrazione interna
            createNewFragment(new RegistrationFragment());
        }

    }

    public void createNewFragment(Fragment fragment) {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        transaction.add(R.id.FrameLayoutFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //Nasconde la tastiera alla pressione di un elemento che non sia essa stessa o una text box
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
