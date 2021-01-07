package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.ResetPasswordActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ResetPasswordController {
    private static ResetPasswordController instance;
    private ResetPasswordActivity resetPasswordActivity;

    private ResetPasswordController() {}

    public static ResetPasswordController getResetPasswordControllerInstance() {
        if(instance == null)
            instance = new ResetPasswordController();
        return instance;
    }

    public void setResetPasswordActivity(ResetPasswordActivity resetPasswordActivity) {
        this.resetPasswordActivity = resetPasswordActivity;
    }

    public TextWatcher getEmailTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(resetPasswordActivity == null)
                    return;

                if(!Utilities.isEmailValid(resetPasswordActivity.getEmail())) {
                    resetPasswordActivity.showEmailError();
                    resetPasswordActivity.enableSendInformationButton(false);
                } else
                    resetPasswordActivity.enableSendInformationButton(true);
            }
        };
    }

    public Runnable getEventHandlerForOnClickSendConfirmCode() {
        return () ->{
            if(checkNullActivityOrNoConnection(resetPasswordActivity)) return;

            Amplify.Auth.resetPassword(
                    resetPasswordActivity.getEmail(),
                    result -> {
                        Log.i("changePassword", result.toString() + "\n" + result.isPasswordReset());
                        resetPasswordActivity.disableEmailEditText();
                        resetPasswordActivity.changeTextFromSendInformationButton();
                        resetPasswordActivity.enableSendInformationButton(false);
                        resetPasswordActivity.enableEditTextsForNewPassword();
                        resetPasswordActivity.runOnUiThread(() -> Utilities.stampaToast(resetPasswordActivity,
                                "Inserisci il codice di verifica che hai ricevuto sulla tua mail"));
                    },
                    error -> {
                        Log.e("changePassword", error.toString());
                        resetPasswordActivity.runOnUiThread(() ->
                                Utilities.stampaToast(resetPasswordActivity, "L'email inserita non è stata mai registrata,\nnon è possibile proseguire!!"));
                    }
            );
        };
    }

    public Runnable getEventHandlerForOnClickSendNewPassword() {
        return () ->{
            if(checkNullActivityOrNoConnection(resetPasswordActivity)) return;

            Amplify.Auth.confirmResetPassword(
                    resetPasswordActivity.getNewPassword(),
                    resetPasswordActivity.getConfirmCode(),
                    () -> setNextChangePasswordStep(),
                    error -> {
                        Log.e("confirmResetPassword", error.toString());
                        resetPasswordActivity.runOnUiThread(() ->
                                Utilities.stampaToast(resetPasswordActivity, "L'email inserita non è stata mai registrata,\n non è possibile proseguire!!"));
                    }
            );
        };
    }

    private void setNextChangePasswordStep() {
        if(checkNullActivityOrNoConnection(resetPasswordActivity)) return;

        Log.i("confirmResetPassword", "apparentemente la password è stata cambiata");
        resetPasswordActivity.runOnUiThread(() ->
                Utilities.stampaToast(resetPasswordActivity, "La password è stata cambiata con successo!!"));

        resetPasswordActivity.finish();
    }

    private boolean checkNullActivityOrNoConnection(Activity activity) {
        if(activity == null)
            //TODO: gestire questo caso (non si può chiamare stampaToast poiché activity è null)
            //....
            return true; //null activity

        if(!Utilities.isOnline(activity)) {
            activity.runOnUiThread(() -> Utilities.stampaToast(activity, activity.getApplicationContext().getResources().getString(R.string.networkNotAvailable)));
            return true; //no connection
        }

        return false;
    }

    public TextWatcher getNewPasswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(resetPasswordActivity == null)
                    return;

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

    public View.OnClickListener getMostraPasswordCheckBoxListener() {
        return listener -> {
            //Se la CheckBox è selezionata
            if (resetPasswordActivity.isCheckBoxMostraPasswordEnabled()) {
                // mostra password
                resetPasswordActivity.showOrHidePassword(true);
            } else {
                // nascondi password
                resetPasswordActivity.showOrHidePassword(false);
            }

            resetPasswordActivity.updatePasswordFocus();
        };
    }
}
