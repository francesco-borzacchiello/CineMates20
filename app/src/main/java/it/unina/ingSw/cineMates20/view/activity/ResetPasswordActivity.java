package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.ResetPasswordController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ResetPasswordActivity extends AppCompatActivity {

    //region Attributi
    private ResetPasswordController resetPasswordController;

    private EditText emailEditText,
                     confirmCodeEditText,
                     newPasswordEditText,
                     confirmNewPasswordEditText;

    private Button sendInformationButton;
    private CheckBox mostraPasswordCheckBox;
    //endregion

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeGraphicsComponents();

        resetPasswordController = ResetPasswordController.getResetPasswordControllerInstance();
        resetPasswordController.setResetPasswordActivity(this);

        setAllActionListener();
    }
    //endregion

    //region Inizializzazione dell'Activity
    //region Inizializzazione dei riferimenti ai componenti grafici che interagiscono con la logica
    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_reset_password);

        emailEditText = findViewById(R.id.editTextEmail);
        confirmCodeEditText = findViewById(R.id.editTextConfirmCode);
        newPasswordEditText = findViewById(R.id.editTextNewPassword);
        confirmNewPasswordEditText = findViewById(R.id.editTextConfirmNewPassword);
        sendInformationButton = findViewById(R.id.buttonInviaCodiceResetPassword);
        mostraPasswordCheckBox = findViewById(R.id.mostraPswResetPassword);
    }
    //endregion

    //region Inizializzazione dei gestori di eventi
    private void setAllActionListener() {
        emailEditText.addTextChangedListener(resetPasswordController.getEmailTextWatcher());

        newPasswordEditText.addTextChangedListener(resetPasswordController.getNewPasswordTextWatcher());
        confirmNewPasswordEditText.addTextChangedListener(resetPasswordController.getConfirmNewPasswordTextWatcher());

        sendInformationButton.setOnClickListener(sendConfirmCodeOnClickListener());

        mostraPasswordCheckBox.setOnClickListener(resetPasswordController.getMostraPasswordCheckBoxListener());
    }
    //endregion

    @NotNull
    @Contract(pure = true)
    private OnClickListener sendConfirmCodeOnClickListener() {
        return v -> {
            Runnable eventForSendConfirmCode = resetPasswordController.getEventHandlerForOnClickSendConfirmCode();
            try {
                eventForSendConfirmCode.run();
                sendInformationButton.setOnClickListener(sendNewPasswordOnClickListener());
            } catch (NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile inviare il codice per cambiare la password.\nRiprova tra qualche minuto");
            }
        };
    }

    private OnClickListener sendNewPasswordOnClickListener() {
        return v -> {
            Runnable eventForSendNewPassword = resetPasswordController.getEventHandlerForOnClickSendNewPassword();
            try {
                eventForSendNewPassword.run();
            } catch (NullPointerException e) {
                Utilities.stampaToast(this, "Al momento non è possibile cambiare la password.\nRiprova tra qualche minuto");
            }
        };
    }

    //endregion

    //region Cambiamenti grafici dell'Activity
    //region Nascondere la tastiera al tocco dello schermo, in un aria differente dalla tastiera
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }
    //endregion

    //region Abilita EditText in cui verrà inserita la password
    public void enableEditTextsForNewPassword() {
        runOnUiThread(()-> {
            confirmCodeEditText.setEnabled(true);
            newPasswordEditText.setEnabled(true);
            confirmNewPasswordEditText.setEnabled(true);
        });
    }
    //endregion

    //region Abilita / Disabilita il tasto
    public void enableSendInformationButton(boolean enable){
        runOnUiThread(()-> sendInformationButton.setEnabled(enable));
    }
    //endregion

    //region Cambia il testo contenuto nel tasto
    public void changeTextFromSendInformationButton(){
        runOnUiThread(()-> sendInformationButton.setText(R.string.cambia_password));
    }
    //endregion

    //region Disabilita l'EditText che contiene la mail
    public void disableEmailEditText(){
        runOnUiThread(()-> emailEditText.setEnabled(false));
    }
    //endregion

    //region Mostra Errori
    public void showEmailError() {
        if(emailEditText != null)
            runOnUiThread(()-> emailEditText.setError("Email non valida"));
    }

    public void showNewPasswordError() {
        if(newPasswordEditText != null)
            runOnUiThread(()-> newPasswordEditText.setError("Password non valida"));
    }

    public void showConfirmNewPasswordError() {
        if(confirmNewPasswordEditText != null)
            runOnUiThread(()-> confirmNewPasswordEditText.setError("Le password non coincidono"));
    }
    //endregion

    //region Mostra / Nascondi le password
    public void showOrHidePassword(boolean show) {
        runOnUiThread(()-> {
            if(show) {
                newPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmNewPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else {
                newPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmNewPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }
    //endregion

    public void updatePasswordFocus() {
        runOnUiThread(()-> {
            if(newPasswordEditText.hasFocus())
                newPasswordEditText.setSelection(newPasswordEditText.length());
            else if(confirmNewPasswordEditText.hasFocus())
                confirmNewPasswordEditText.setSelection(confirmNewPasswordEditText.length());
        });
    }

    public void enableMostraPasswordCheckBox() {
        runOnUiThread(()-> mostraPasswordCheckBox.setEnabled(true));
    }
    //endregion

    //region Getter per le informazioni presenti sull'Activity
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
    //endregion
}