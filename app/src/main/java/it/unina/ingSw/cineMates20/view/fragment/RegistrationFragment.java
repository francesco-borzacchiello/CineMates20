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
import it.unina.ingSw.cineMates20.view.util.InternetStatus;
import it.unina.ingSw.cineMates20.view.util.Utilities;

/** Fragment che si occupa di mostrare le TextBox necessarie all'inserimento
  * dei dati di un utente che si registra internamente al sistema. */
public class RegistrationFragment extends Fragment {

    private EditText nome;
    private EditText cognome;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confermaPassword;
    private Button registerButton;
    private TextWatcher afterTextChangedListener;
    private Context context;

    public RegistrationFragment() {
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
        setEditText(view);

        registerButton = view.findViewById(R.id.registratiButton);

        if(isAdded() && getActivity() != null)
            context = getActivity().getApplicationContext();
        else return;

        //Questo listener dipende dal fragment, e per tale ragione non può essere spostato nell'activity contenitrice
        registerButton.setOnClickListener(listener -> {
            //TODO: confermare che chiamare LoginController da qui vada bene (serve un RegistrationController?)
            if(context != null && !InternetStatus.getInstance().isOnline(context)) {
                LoginController.stampaMessaggioToast(context,"Connessione ad internet non disponibile!");
                return;
            }

            /* Nome e cognome sono per forza validi in quanto è soltanto richiesto che siano non vuoti,
             * e il tasto registrati viene disabilitato non appena una EditText diventa vuota. */

            if (!Utilities.isUserNameValid(username.getText().toString())) {
                LoginController.stampaMessaggioToast(context, "L'username inserito non è valido oppure è già in uso.");
                return;
            }
            if (!Utilities.isEmailValid(email.getText().toString())) {
                LoginController.stampaMessaggioToast(context, "L'email inserita non è valida oppure è già in uso.");
                return;
            }
            if (!Utilities.isPasswordValid(password.getText().toString())) {
                LoginController.stampaMessaggioToast(context, "La password deve contenere almeno un numero, un carattere speciale, una lettera minuscola e una maiuscola.");
                return;
            }
            if (!Utilities.isConfirmPasswordValid(password.getText().toString(), confermaPassword.getText().toString())) {
                LoginController.stampaMessaggioToast(context, "Le password non coincidono!");
                return;
            }

            if(!LoginController.isConnectedToInternet(context)) {
                LoginController.stampaMessaggioToast(context, "Connessione ad internet non disponibile!");
                return;
            }

            /*TODO: Se non è stata modificata foto, passare url foto default a Cognito.
             *      Procedere con la registrazione (mostrare prima ConfirmRegistrationCodeFragment) */
            //...
        });

        afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                //Se tutte le EditText sono non vuote, abilita il tasto Registrati
                registerButton.setEnabled(allEditTextAreNotEmpty());
            }
        };

        addTextChangedListenerToEditTexts();
    }

    //Restituisce true se tutte le EditText sono non vuote al momento dell'invocazione
    public boolean allEditTextAreNotEmpty() {
        return nome.getText().toString().trim().length() > 0 && cognome.getText().toString().trim().length() > 0
               && username.getText().toString().trim().length() > 0 && email.getText().toString().trim().length() > 0
               && password.getText().toString().trim().length() > 0 && confermaPassword.getText().toString().trim().length() > 0;
    }

    private void setEditText(@NotNull View v) {
        nome = v.findViewById(R.id.nomeRegistrazione);
        cognome = v.findViewById(R.id.cognomeRegistrazione);
        password = v.findViewById(R.id.passwordRegistrazione);
        confermaPassword = v.findViewById(R.id.confermaPasswordRegistrazione);
        email = v.findViewById(R.id.emailRegistrazione);
        username = v.findViewById(R.id.usernameRegistrazione);
    }

    private void addTextChangedListenerToEditTexts() {
        nome.addTextChangedListener(afterTextChangedListener);
        cognome.addTextChangedListener(afterTextChangedListener);
        email.addTextChangedListener(afterTextChangedListener);
        password.addTextChangedListener(afterTextChangedListener);
        confermaPassword.addTextChangedListener(afterTextChangedListener);
        username.addTextChangedListener(afterTextChangedListener);
    }
}