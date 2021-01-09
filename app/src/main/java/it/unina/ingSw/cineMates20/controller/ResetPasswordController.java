package it.unina.ingSw.cineMates20.controller;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.view.activity.ResetPasswordActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ResetPasswordController {

    //region Attributi
    private static ResetPasswordController instance;
    private ResetPasswordActivity resetPasswordActivity;
    //endregion

    //region Costruttore
    private ResetPasswordController() {}
    //endregion

    //region getIstance() per il pattern singleton
    public static ResetPasswordController getResetPasswordControllerInstance() {
        if(instance == null)
            instance = new ResetPasswordController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'activity gestita da questo controller
    public void setResetPasswordActivity(ResetPasswordActivity resetPasswordActivity) {
        this.resetPasswordActivity = resetPasswordActivity;
    }
    //endregion

    //region Getter dei gestori degli eventi
    //region Costruzione dei gestori di eventi per inviare informazioni alla pressione di un tasto
    //region Invio del codice di conferma alla pressione del tasto "invio", con annessi aggiornamenti grafici
    public Runnable getEventHandlerForOnClickSendConfirmCode() {
        return () ->{
            if(Utilities.checkNullActivityOrNoConnection(resetPasswordActivity)) return;

            Amplify.Auth.resetPassword(
                    resetPasswordActivity.getEmail(),
                    result -> {
                        Log.i("changePassword", result.toString() + "\n" + result.isPasswordReset());
                        resetPasswordActivity.disableEmailEditText();
                        resetPasswordActivity.changeTextFromSendInformationButton();
                        resetPasswordActivity.enableSendInformationButton(false);
                        resetPasswordActivity.enableEditTextsForNewPassword();
                        resetPasswordActivity.enableMostraPasswordCheckBox();
                        resetPasswordActivity.runOnUiThread(() -> Utilities.stampaToast(resetPasswordActivity,
                                "Inserisci il codice di verifica che hai ricevuto via mail"));
                    },
                    error -> {
                        Log.e("changePassword", error.toString());
                        resetPasswordActivity.runOnUiThread(() ->
                                Utilities.stampaToast(resetPasswordActivity, "L'email inserita non è registrata,\nnon è possibile proseguire!"));
                    }
            );
        };
    }
    //endregion

    //region Modifica della password alla pressione del tasto "invio"
    public Runnable getEventHandlerForOnClickSendNewPassword() {
        return () ->{
            if(Utilities.checkNullActivityOrNoConnection(resetPasswordActivity)) return;

            Amplify.Auth.confirmResetPassword(
                    resetPasswordActivity.getNewPassword(),
                    resetPasswordActivity.getConfirmCode(),
                    this::setNextChangePasswordStep,
                    error -> {
                        Log.e("confirmResetPassword", error.toString());
                        resetPasswordActivity.runOnUiThread(() ->
                                Utilities.stampaToast(resetPasswordActivity, "L'email inserita non è stata mai registrata,\n non è possibile proseguire!!"));
                    }
            );
        };
    }
    //endregion

    //region Verifica la connessione, se è disponibile comunica all'utente che la password è stata correttamente cambiata
    private void setNextChangePasswordStep() {
        if(Utilities.checkNullActivityOrNoConnection(resetPasswordActivity)) return;

        Log.i("confirmResetPassword", "apparentemente la password è stata cambiata");
        resetPasswordActivity.runOnUiThread(() ->
                Utilities.stampaToast(resetPasswordActivity, "La password è stata cambiata con successo!!"));

        resetPasswordActivity.finish();
    }
    //endregion
    //endregion

    //region TextWatcher che compiono un'azione al seguito della modifica del testo nelle TextView
    //region Al variare dello stato di validazione dell'email si attiva o meno il tasto per inviare il codice per cambiare la password
    public TextWatcher getEmailTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(resetPasswordActivity == null) return;

                if(!Utilities.isEmailValid(resetPasswordActivity.getEmail())) {
                    resetPasswordActivity.showEmailError();
                    resetPasswordActivity.enableSendInformationButton(false);
                } else resetPasswordActivity.enableSendInformationButton(true);
            }
        };
    }
    //endregion

    //region Al variare dello stato di validazione della password viene abilitato o meno il tasto per cambiare la password
    public TextWatcher getNewPasswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(resetPasswordActivity == null) return;

                if(!Utilities.isPasswordValid(resetPasswordActivity.getNewPassword())) {
                    resetPasswordActivity.showNewPasswordError();
                    resetPasswordActivity.enableSendInformationButton(false);
                } else if(Utilities.isPasswordValid(resetPasswordActivity.getConfirmNewPassword()) &&
                            Utilities.isConfirmPasswordValid(resetPasswordActivity.getNewPassword(), resetPasswordActivity.getConfirmNewPassword()))
                        resetPasswordActivity.enableSendInformationButton(true);
                    /*else
                        resetPasswordActivity.runOnUiThread(() -> Utilities.stampaToast(resetPasswordActivity,
                                "La password deve avere lunghezza minima 8 caratteri,\ncontenere almeno un carattere speciale,\n" +
                                        "almeno una lettera maiuscola e almeno una minuscola\ne almeno un numero"));*/
            }
        };
    }

    public TextWatcher getConfirmNewPasswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(resetPasswordActivity == null)
                    return;

                if(!Utilities.isPasswordValid(resetPasswordActivity.getConfirmNewPassword())) {
                    resetPasswordActivity.showConfirmNewPasswordError();
                    resetPasswordActivity.enableSendInformationButton(false);
                } else if(Utilities.isPasswordValid(resetPasswordActivity.getNewPassword()) &&
                        Utilities.isConfirmPasswordValid(resetPasswordActivity.getNewPassword(), resetPasswordActivity.getConfirmNewPassword()))
                    resetPasswordActivity.enableSendInformationButton(true);
                /*else
                    resetPasswordActivity.runOnUiThread(() -> Utilities.stampaToast(resetPasswordActivity,
                            "La password deve avere lunghezza minima 8 caratteri,\ncontenere almeno un carattere speciale,\n" +
                                    "almeno una lettera maiuscola e almeno una minuscola\ne almeno un numero"));*/
            }
        };
    }
    //endregion
    //endregion

    //region Mostrare / Nascondere la password, al selezionare / deselezionare di una checkbox
    public View.OnClickListener getMostraPasswordCheckBoxListener() {
        return listener -> {
            //Se la CheckBox è selezionata mostra la password, altrimenti la nasconde
            resetPasswordActivity.showOrHidePassword(resetPasswordActivity.isCheckBoxMostraPasswordEnabled());

            resetPasswordActivity.updatePasswordFocus();
        };
    }
    //endregion
    //endregion
}
