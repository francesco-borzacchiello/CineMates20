package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.LoginController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class LoginActivity extends AppCompatActivity {

    //region Attributi
    private LoginController loginController;

    private EditText usernameEditText,
                     passwordEditText;
    private Button loginButton;
    private TextView creaNuovoAccount,
                     passwordDimenticata;
    private ImageView googleLogo,
                      facebookLogo,
                      twitterLogo;
    private CheckBox mostraPassword;
    //endregion

    //region onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginController = LoginController.getLoginControllerInstance();
        loginController.setLoginActivity(this);

        initializeGraphicsComponents();
        setAllActionListener();
    }
    //endregion

    private void setAllActionListener() {
        TextWatcher usernameTextChangedListener = loginController.getUsernameLoginTextWatcher();
        TextWatcher passwordTextChangedListener = loginController.getPasswordLoginTextWatcher();

        usernameEditText.addTextChangedListener(usernameTextChangedListener);
        passwordEditText.addTextChangedListener(passwordTextChangedListener);

        loginButton.setOnClickListener(loginOnClickListener());
        mostraPassword.setOnClickListener(loginController.getMostraPasswordCheckBoxListener());

        creaNuovoAccount.setOnClickListener(registrationOnClickListener(false, null));
        googleLogo.setOnClickListener(registrationOnClickListener(true, "google"));
        facebookLogo.setOnClickListener(registrationOnClickListener(true, "facebook"));
        twitterLogo.setOnClickListener(registrationOnClickListener(true, "twitter"));

        passwordDimenticata.setOnClickListener(passwordDimenticataOnClickListener());
    }

    //region Getter
    public String getUsername() {
        if(usernameEditText != null)
            return usernameEditText.getText().toString().trim();
        return null;
    }

    public String getPassword() {
        if(passwordEditText != null)
            return passwordEditText.getText().toString().trim();
        return null;
    }
    //endregion

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener loginOnClickListener() {
        return v -> {
            Runnable eventForLoginClick = loginController.getEventHandlerForOnClickLogin(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString());
            try {
                eventForLoginClick.run();
            } catch(NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile effettuare il login.\nRiprova tra qualche minuto");
            }
        };
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener registrationOnClickListener(boolean isSocialLogin, String socialProvider) {
        return v -> {
            Runnable eventForCreateNewUser = loginController.getEventHandlerForOnClickRegistration(isSocialLogin, socialProvider);

            try {
                eventForCreateNewUser.run();
            } catch(NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile creare un nuovo account.\nRiprova tra qualche minuto");
                //return;
            }
        };
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener passwordDimenticataOnClickListener() {
        return v -> {
            Runnable eventForResetPassword = loginController.getEventHandlerForOnClickResetPassword();

            try {
                eventForResetPassword.run();
            } catch(NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile cambiare la password.\nRiprova tra qualche minuto");
            }
        };
    }

    private void initializeGraphicsComponents(){
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.usernameLogin);
        passwordEditText = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.loginButton);
        creaNuovoAccount = findViewById(R.id.nuovoAccount);
        passwordDimenticata = findViewById(R.id.passwordDimenticata);
        googleLogo = findViewById(R.id.googleLogo);
        facebookLogo = findViewById(R.id.facebookLogo);
        twitterLogo = findViewById(R.id.twitterLogo);
        mostraPassword = findViewById(R.id.mostraPswLogin);
    }

    //Nasconde la tastiera alla pressione di un elemento che non sia essa stessa o una text box
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }

    public void showUsernameError() {
        if(usernameEditText != null)
            usernameEditText.setError("Email o username non valido");
    }

    public void showPasswordError() {
        if(passwordEditText != null)
            passwordEditText.setError("La password non è valida");
    }

    public void enableLoginButton(boolean enable) {
        if(loginButton != null)
            loginButton.setEnabled(enable);
    }

    public boolean isCheckBoxMostraPasswordEnabled() {
        if(mostraPassword != null)
            return mostraPassword.isChecked();
        return false;
    }

    public void showOrHidePassword(boolean show) {
        if(show) {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    public void updatePasswordFocus() {
        if(passwordEditText != null && passwordEditText.hasFocus())
            passwordEditText.setSelection(passwordEditText.length());
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