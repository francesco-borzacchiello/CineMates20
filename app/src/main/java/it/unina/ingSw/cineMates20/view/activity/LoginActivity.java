package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    protected void onCreate(Bundle savedInstanceState) {
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

        creaNuovoAccount.setOnClickListener(getSignUpOnClickListener(false, null));
        googleLogo.setOnClickListener(getSignUpOnClickListener(true, "google"));
        facebookLogo.setOnClickListener(getSignUpOnClickListener(true, "facebook"));
        twitterLogo.setOnClickListener(getSignUpOnClickListener(true, "twitter"));

        passwordDimenticata.setOnClickListener(getPasswordDimenticataOnClickListener());
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
    private OnClickListener loginOnClickListener() {
        return v -> {
            Runnable eventForLoginClick = loginController.getEventHandlerForOnClickLogin();
            try {
                eventForLoginClick.run();
            } catch(NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile effettuare il login.\nRiprova tra qualche minuto");
            }
        };
    }

    @NotNull
    @Contract(pure = true)
    private OnClickListener getSignUpOnClickListener(boolean isSocialLogin, String socialProvider) {
        return v -> {
            Runnable eventForCreateNewUser = loginController.getEventHandlerForOnClickRegistration(isSocialLogin, socialProvider);

            try {
                eventForCreateNewUser.run();
            } catch(NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile creare un nuovo account.\nRiprova tra qualche minuto");
            }
        };
    }

    @NotNull
    @Contract(pure = true)
    private OnClickListener getPasswordDimenticataOnClickListener() {
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
            runOnUiThread(()-> usernameEditText.setError("Email o username non valido"));
    }

    public void showPasswordError() {
        if(passwordEditText != null)
            runOnUiThread(()-> passwordEditText.setError("La password non è valida"));
    }

    public void enableLoginButton(boolean enable) {
        if(loginButton != null)
            runOnUiThread(()-> loginButton.setEnabled(enable));
    }

    public boolean isCheckBoxMostraPasswordEnabled() {
        if(mostraPassword != null)
            return mostraPassword.isChecked();
        return false;
    }

    public void showOrHidePassword(boolean show) {
        runOnUiThread(()-> {
            if (show) {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    public void updatePasswordFocus() {
        if(passwordEditText != null && passwordEditText.hasFocus())
            passwordEditText.setSelection(passwordEditText.length());
    }
}