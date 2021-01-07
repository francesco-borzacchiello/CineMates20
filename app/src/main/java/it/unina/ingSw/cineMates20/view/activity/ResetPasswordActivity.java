package it.unina.ingSw.cineMates20.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.ResetPasswordController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ResetPasswordActivity extends AppCompatActivity {

    private ResetPasswordController resetPasswordController;

    private EditText emailEditText,
                     confirmCodeEditText,
                     newPasswordEditText,
                     confirmNewPasswordEditText;

    private Button sendInformationButton;
    private CheckBox mostraPasswordCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGraphicsComponents();

        resetPasswordController = ResetPasswordController.getResetPasswordControllerInstance();
        resetPasswordController.setResetPasswordActivity(this);

        setAllActionListener();
    }

    private void initializeGraphicsComponents() {
        Objects.requireNonNull(getSupportActionBar()).hide(); //Nasconde la barra del titolo - chiamare questo metodo prima di setContentView
        setContentView(R.layout.activity_reset_password);

        emailEditText = findViewById(R.id.editTextEmail);
        confirmCodeEditText = findViewById(R.id.editTextConfirmCode);
        newPasswordEditText = findViewById(R.id.editTextNewPassword);
        confirmNewPasswordEditText = findViewById(R.id.editTextConfirmNewPassword);
        sendInformationButton = findViewById(R.id.buttonInviaCodiceResetPassword);
        mostraPasswordCheckBox = findViewById(R.id.mostraPswResetPassword);
    }

    private void setAllActionListener() {
        TextWatcher emailTextChangedListener = resetPasswordController.getEmailTextWatcher();
        emailEditText.addTextChangedListener(emailTextChangedListener);

        TextWatcher newPasswordTextChangedListener = resetPasswordController.getNewPasswordTextWatcher();
        newPasswordEditText.addTextChangedListener(newPasswordTextChangedListener);

        TextWatcher confirmNewPasswordTextChangedListener = resetPasswordController.getConfirmNewPasswordTextWatcher();
        confirmNewPasswordEditText.addTextChangedListener(confirmNewPasswordTextChangedListener);

        sendInformationButton.setOnClickListener(sendConfirmCodeOnClickListener());

        mostraPasswordCheckBox.setOnClickListener(resetPasswordController.getMostraPasswordCheckBoxListener());
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener sendConfirmCodeOnClickListener() {
        return v -> {
            Runnable eventForSendConfirmCode = resetPasswordController.getEventHandlerForOnClickSendConfirmCode();
            try {
                eventForSendConfirmCode.run();
            } catch (NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile inviare il codice per cambiare la password.\nRiprova tra qualche minuto");
            }
            sendInformationButton.setOnClickListener(sendNewPasswordOnClickListener());
        };
    }

    private View.OnClickListener sendNewPasswordOnClickListener() {
        return v -> {
            Runnable eventForSendNewPassword = resetPasswordController.getEventHandlerForOnClickSendNewPassword();
            try {
                eventForSendNewPassword.run();
            } catch (NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile cambiare la password.\nRiprova tra qualche minuto");
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }

    public void enableEditTextsForNewPassword(){
        confirmCodeEditText.setEnabled(true);
        newPasswordEditText.setEnabled(true);
        confirmNewPasswordEditText.setEnabled(true);
    }

    public void enableSendInformationButton(boolean enabled){
        sendInformationButton.setEnabled(enabled);
    }

    public void changeTextFromSendInformationButton(){
        sendInformationButton.setText("Cambia Password");
    }

    public void disableEmailEditText(){
        emailEditText.setEnabled(false);
    }

    public void showEmailError() {
        if(emailEditText != null)
            emailEditText.setError("Email non valida");
    }

    public void showNewPasswordError() {
        if(newPasswordEditText != null)
            newPasswordEditText.setError("Password non valida");
    }

    public void showConfirmNewPasswordError() {
        if(confirmNewPasswordEditText != null)
            confirmNewPasswordEditText.setError("Le password non coincidono");
    }

    public String getEmail() {
        return emailEditText.getText().toString().trim();
    }

    public String getNewPassword() {
        return newPasswordEditText.getText().toString().trim();
    }

    public String getConfirmNewPassword() {
        return confirmNewPasswordEditText.getText().toString().trim();
    }

    public String getConfirmCode() {
        return confirmCodeEditText.getText().toString().trim();
    }

    public boolean isCheckBoxMostraPasswordEnabled() {
        return mostraPasswordCheckBox.isChecked();
    }

    public void showOrHidePassword(boolean show) {
        if(show) {
            newPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            confirmNewPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        else {
            newPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            confirmNewPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    public void updatePasswordFocus() {
        if(newPasswordEditText.hasFocus())
            newPasswordEditText.setSelection(newPasswordEditText.length());
        else if(confirmNewPasswordEditText.hasFocus())
            confirmNewPasswordEditText.setSelection(confirmNewPasswordEditText.length());
    }
}