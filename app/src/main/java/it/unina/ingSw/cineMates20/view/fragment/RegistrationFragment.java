package it.unina.ingSw.cineMates20.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.LoginController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

/** Fragment che si occupa di mostrare le TextBox necessarie
  * all'inserimento dei dati di un utente che si registra. */

public class RegistrationFragment extends Fragment {

    private final String nome, cognome;
    private EditText nomeEditText;
    private EditText cognomeEditText;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confermaPasswordEditText;
    private Button registrationButton;
    private TextWatcher afterTextChangedListener;
    private Context context;

    public RegistrationFragment() {
        nome = null; cognome = null;
    }

    public RegistrationFragment(String nome, String cognome) {
        this.nome = nome;
        this.cognome = cognome;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Alla pressione del tasto indietro, distruggi Activity padre RegistrationActivity
        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                if(isAdded() && getActivity() != null)
                    getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    //Questo metodo viene chiamato dopo onCreate()
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {

        if(isAdded() && getActivity() != null)
            context = getActivity().getApplicationContext();
        else return;

        setGraphicsComponents(view);

        //Questo listener dipende dal fragment, e per tale ragione non può essere spostato nell'activity contenitrice
        registrationButton.setOnClickListener(listener -> {
            //TODO: confermare che chiamare LoginController da qui vada bene (serve un RegistrationController?)
            if(context != null && !Utilities.isOnline(context)) {
                LoginController.stampaMessaggioToast(context, "Connessione ad internet non disponibile!");
                return;
            }

            /* Nome e cognome sono per forza validi in quanto è soltanto richiesto che siano non vuoti,
             * e il tasto registrati viene disabilitato non appena una EditText diventa vuota. */

            if (!Utilities.isUserNameValid(usernameEditText.getText().toString())) {
                LoginController.stampaMessaggioToast(context, "L'username inserito non è valido oppure è già in uso.");
                return;
            }

            if(nome==null || cognome == null) {
                if (!Utilities.isEmailValid(emailEditText.getText().toString())) {
                    LoginController.stampaMessaggioToast(context, "L'email inserita non è valida oppure è già in uso.");
                    return;
                }
                if (!Utilities.isPasswordValid(passwordEditText.getText().toString())) {
                    LoginController.stampaMessaggioToast(context, "La password deve contenere almeno un numero, un carattere speciale, una lettera minuscola e una maiuscola.");
                    return;
                }
                if (!Utilities.isConfirmPasswordValid(passwordEditText.getText().toString(), confermaPasswordEditText.getText().toString())) {
                    LoginController.stampaMessaggioToast(context, "Le password non coincidono!");
                    return;
                }
            }

            /*TODO: Se non è stata modificata foto, passare url foto default a Cognito.
             *      Procedere con la registrazione (dire a RegistrationActivity di mostrare
             *      ConfirmRegistrationCodeFragment se questo è login non social, nome==null || cognome==null) */
            //...
            LoginController.stampaMessaggioToast(context, "Funzionalità in sviluppo!");
        });

        afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                //Se tutte le EditText sono non vuote, abilita il tasto Registrati
                registrationButton.setEnabled(allEditTextAreNotEmpty());
            }
        };

        addTextChangedListenerToEditTexts();
    }

    //Restituisce true se tutte le EditText sono non vuote al momento dell'invocazione
    public boolean allEditTextAreNotEmpty() {
        if(nome == null || cognome == null)
            return nomeEditText.getText().toString().trim().length() > 0 && cognomeEditText.getText().toString().trim().length() > 0
                && usernameEditText.getText().toString().trim().length() > 0 && emailEditText.getText().toString().trim().length() > 0
                && passwordEditText.getText().toString().trim().length() > 0 && confermaPasswordEditText.getText().toString().trim().length() > 0;
        else
            return nomeEditText.getText().toString().trim().length() > 0 && cognomeEditText.getText().toString().trim().length() > 0
                    && usernameEditText.getText().toString().trim().length() > 0;
    }

    private void setGraphicsComponents(@NotNull View v) {
        nomeEditText = v.findViewById(R.id.nomeRegistrazione);
        cognomeEditText = v.findViewById(R.id.cognomeRegistrazione);
        usernameEditText = v.findViewById(R.id.usernameRegistrazione);
        passwordEditText = v.findViewById(R.id.passwordRegistrazione);
        confermaPasswordEditText = v.findViewById(R.id.confermaPasswordRegistrazione);
        emailEditText = v.findViewById(R.id.emailRegistrazione);
        registrationButton = v.findViewById(R.id.registratiButton);

        if(nome != null || cognome != null) { //Si nascondono le EditText non necessarie
            passwordEditText.setVisibility(View.GONE);
            confermaPasswordEditText.setVisibility(View.GONE);
            emailEditText.setVisibility(View.GONE);
        }
    }

    private void addTextChangedListenerToEditTexts() {
        nomeEditText.addTextChangedListener(afterTextChangedListener);
        cognomeEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.addTextChangedListener(afterTextChangedListener);

        if(nome == null || cognome == null) {
            emailEditText.addTextChangedListener(afterTextChangedListener);
            passwordEditText.addTextChangedListener(afterTextChangedListener);
            confermaPasswordEditText.addTextChangedListener(afterTextChangedListener);
        }
    }
}