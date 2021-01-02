package it.unina.ingSw.cineMates20.view.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.util.IdentifiedForEventHandlers;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private Runnable eventForLoginClick,
                     eventForCreateNewUser;

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;
    private TextView creaNuovoAccount;
    private ImageView googleLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Objects.requireNonNull(getSupportActionBar()).hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView

        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        initializeGraphicsComponents();

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
                Utilities.stampaToast(getApplicationContext(), loginResult.getError().toString());
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
            if(!Utilities.isOnline(getApplicationContext())) {
                Utilities.stampaToast(getApplicationContext(),
                        getApplicationContext().getResources().getString(R.string.networkNotAvailable));
                return;
            }

            try {
                //Estraggo Runnable per il listener del pulsante login
                Utilities.Srunnable s = (Utilities.Srunnable) getIntent().getSerializableExtra(IdentifiedForEventHandlers.ON_CLICK_LOGIN);
                this.eventForLoginClick = s.getRunnable();
                eventForLoginClick.run();
            } catch(NullPointerException e) {
                Utilities.stampaToast(getApplicationContext(), "Al momento non è possibile effettuare il login.\nRiprova tra qualche minuto");
                return;
            }

            loadingProgressBar.setVisibility(View.VISIBLE);
            loginViewModel.login(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
        });

        creaNuovoAccount.setOnClickListener(registrationOnClickListener(false, null));
        googleLogo.setOnClickListener(registrationOnClickListener(true, "google"));
    }

    private View.OnClickListener registrationOnClickListener(boolean isSocialLogin, String socialProvider) {
        return v -> {
            if (!Utilities.isOnline(getApplicationContext())) {
                Utilities.stampaToast(getApplicationContext(),
                        getApplicationContext().getResources().getString(R.string.networkNotAvailable));
                return;
            }

            Intent myIntent = new Intent(LoginActivity.this, RegistrationActivity.class);

            myIntent.putExtra("isSocialLogin", isSocialLogin);
            myIntent.putExtra("socialProvider", socialProvider);

            LoginActivity.this.startActivity(myIntent);
            //Non lanciare finish() in quanto in caso di pressione di tasto indietro si verrà reindirizzati qui*/
        };
    }

    private void initializeGraphicsComponents(){
        Objects.requireNonNull(getSupportActionBar()).hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.usernameLogin);
        passwordEditText = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.loginButton);
        loadingProgressBar = findViewById(R.id.loading);
        creaNuovoAccount = findViewById(R.id.nuovoAccount);
        googleLogo = findViewById(R.id.googleLogo);
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

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + " " + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Utilities.stampaToast(getApplicationContext(), welcome);
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