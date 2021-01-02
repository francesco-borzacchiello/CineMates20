package it.unina.ingSw.cineMates20.view.activity;

import it.unina.ingSw.cineMates20.view.fragment.RegistrationFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import it.unina.ingSw.cineMates20.R;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView
        setContentView(R.layout.activity_registration);

        boolean isSocialRegistration = false;
        String socialProviderRegistration;

        Bundle loginBundle = getIntent().getExtras();
        if(loginBundle != null) {
            isSocialRegistration = loginBundle.getBoolean("isSocialLogin");
            socialProviderRegistration = loginBundle.getString("socialProvider");
        }
        else //in caso di loginType null si verrà reindirizzati alla pagina precedente
            finish();

        if(isSocialRegistration) {
            //TODO: aggiungere reindirizzamento pagina social, estrarre tutti i dati, inserirli, ritornare alla app
            //... if(socialProviderRegistration == "google") ...
            //Si mostra il fragment per la registrazione social con i campi "Nome" e "Cognome" già inizializzati
            //....nome = getSocialUserName(); cognome = getSocialUserSurname();
            createNewFragment(new RegistrationFragment("Carmine","Grimaldi"));
        }
        else { //Si mostra il fragment per la registrazione interna
            createNewFragment(new RegistrationFragment());
        }

    }

    public void createNewFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.FrameLayoutFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //Nasconde la tastiera alla pressione di un elemento che non sia essa stessa o una text box
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int[] screenCords = new int[2];
            w.getLocationOnScreen(screenCords);
            float x = event.getRawX() + w.getLeft() - screenCords[0];
            float y = event.getRawY() + w.getTop() - screenCords[1];

            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

}
