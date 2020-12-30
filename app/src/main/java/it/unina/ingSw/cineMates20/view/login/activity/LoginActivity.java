package it.unina.ingSw.cineMates20.view.login.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.login.util.InternetStatus;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private Runnable eventForLoginClick;
    private boolean isLoggedIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView

        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.usernameLogin);
        final EditText passwordEditText = findViewById(R.id.passwordLogin);
        final Button loginButton = findViewById(R.id.loginButton);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final TextView creaNuovoAccount = findViewById(R.id.nuovoAccount);

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                updateUiWithUser(loginResult.getSuccess());
            }
            setResult(Activity.RESULT_OK);

            //Complete and destroy login activity once successful
            finish();
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            if(!InternetStatus.getInstance(getApplicationContext()).isOnline()) {
                Toast.makeText(getApplicationContext(), "Connessione ad internet non disponibile!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                //TODO: Aggiungere logica login in setNextStep(), se il login va a buon fine (result.isSignInComplete() == true), reindirizzare alla Home, altrimenti mostrare errore a schermo
                //Nota: in caso di wifi spento, il login fallisce semplicemente.
                Amplify.Auth.signIn(
                        "carmineG", //TODO: sostituire con credenziali date in input
                        "Carmine_97",
                        result -> setNextStep(result.isSignInComplete()),
                        error -> Log.e("AuthQuickstart", error.toString())
                );
            } catch (Exception error) {
                Log.e("AuthQuickstart", "Could not initialize Amplify", error);
            }

            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(usernameEditText.getText().toString(),
                   passwordEditText.getText().toString());
        });

        creaNuovoAccount.setOnClickListener(v -> {
            Intent myIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
            myIntent.putExtra("loginType", false);
            LoginActivity.this.startActivity(myIntent);
            //Non lanciare finish() in quanto in caso di pressione di tasto indietro si verrà reindirizzati qui
        });

    }

    //TODO: Se il login è fallito allora mostra errori a schermo, altrimenti passa alla prossima activity, chiama clearBackStack() e infine finish()
    //Nota: questo metodo sembra venga chiamato soltanto dopo il termine del corpo di loginButton.setOnClickListener()
    private void setNextStep(boolean signIn) {
        //...
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

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    //Metodo che viene chiamato al termine del login (quando ha successo), consente di svuotare il backStack
    private void clearBackStack() {
        FragmentManager fm = getFragmentManager(); // or 'getSupportFragmentManager();'
        int count = fm.getBackStackEntryCount();
        for(int i = 0; i < count; ++i) {
            fm.popBackStack();
        }
    }
}

                /*CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        "eu-west-3:9dfb8e38-d56c-47a4-a10d-2b412bdad408", // ID pool di identità
                        Regions.EU_WEST_3 // Regione
                );*/

                /*//Codice per fare logout
                Amplify.Auth.signOut(
                        () -> Log.i("AuthQuickstart", "Signed out successfully"),
                        error -> Log.e("AuthQuickstart", error.toString())
                );*/


                /* //Codice per registrarsi, NOTA: gli attributi sottostanti sono tutti obbligatori
                ArrayList<AuthUserAttribute> attributes = new ArrayList<>();
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.email(), "carmineegr@gmail.com"));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.preferredUsername(), "carmineG"));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.familyName(), "Carmine Grimaldi"));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.name(), "Carmine"));
                attributes.add(new AuthUserAttribute(AuthUserAttributeKey.picture(), "null"));

                Amplify.Auth.signUp(
                        "carmineG",
                        "Carmine_97",
                        AuthSignUpOptions.builder().userAttributes(attributes).build(),
                        result -> Log.i("AuthQuickStart", "Result: " + result.toString()),
                        error -> Log.e("AuthQuickStart", "Sign up failed", error)
                );
                 */

                /* //Codice per confermare l'email
                Amplify.Auth.confirmSignUp(
                        "carmineG",
                        "706626",
                        result -> Log.i("AuthQuickstart", result.toString()),
                        error -> Log.e("AuthQuickstart", error.toString())
                );
                */

                /*Amplify.Auth.fetchAuthSession(
                        result -> Log.i("AmplifyQuickstart", "login: " + result.toString()),
                        error -> Log.e("AmplifyQuickstart", "login: " + error.toString())
                );*/

                //Codice per fare login con username e password:
                /*Amplify.Auth.signIn(
                        "carmineG",
                        "Carmine_97",
                        result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                        error -> Log.e("AuthQuickstart", error.toString())
                );*/

                /* //Codice per fare login con email e password:
                Amplify.Auth.signIn(
                        "carmineegr@gmail.com", //L'email può essere inserita al posto di "username"
                        "Carmine_97",
                        result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                        error -> Log.e("AuthQuickstart", error.toString())
                );*/