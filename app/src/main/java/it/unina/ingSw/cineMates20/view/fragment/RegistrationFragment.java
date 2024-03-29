package it.unina.ingSw.cineMates20.view.fragment;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;

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
    private CheckBox checkBoxMostraPassword;
    private Runnable registrationButtonRunnable;
    private OnClickListener mostraPasswordListener,
                                 loadImageListener;
    private ImageView profileImage,
                      pencilToLoadImage;
    private Bitmap profileImageBitmap;

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

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setMessage("Sei sicuro di voler annullare?")
                        .setCancelable(false)
                        .setPositiveButton("Si", (dialog, id) ->  {
                            if(isAdded() && getActivity() != null) {
                                getActivity().getSupportFragmentManager().popBackStack();
                                getActivity().finish();
                            }
                         })
                        .setNegativeButton("No", null)
                        .show();

                alertDialog.setOnKeyListener((arg0, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        alertDialog.dismiss();
                    }
                    return true;
                });
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    //Questo metodo viene chiamato dopo onCreate()
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        setGraphicsComponents(view);

        registrationButton.setOnClickListener(listener -> {
            if(getRegistrationButtonRunnable() != null)
                getRegistrationButtonRunnable().run();
        });

        //funzione che gestisce il click all'interno della CheckBox
        if(nome == null || cognome == null)
            checkBoxMostraPassword.setOnClickListener(mostraPasswordListener);

        addTextChangedListenerToEditTexts();
        addListenerToLoadImage();
    }

    private void addListenerToLoadImage() {
        pencilToLoadImage.setOnClickListener(loadImageListener);
        profileImage.setOnClickListener(loadImageListener);
    }

    public void setListenerToLoadImage(OnClickListener listenerToLoadImage) {
        loadImageListener = listenerToLoadImage;
    }

    //region Getter e Setter
    public void setAbilitaRegistrazioneTextWatcher(TextWatcher txtw) {
        this.afterTextChangedListener = txtw;
    }

    public void setMostraPasswordCheckBoxListener(OnClickListener listener) {
        mostraPasswordListener = listener;
    }

    public void setRegistrationButtonRunnable(Runnable runnable) {
        registrationButtonRunnable = runnable;
    }

    public Runnable getRegistrationButtonRunnable() {
        return registrationButtonRunnable;
    }

    public void setProfileImageBitmap(Bitmap profileImageBitmap) {
        this.profileImageBitmap = profileImageBitmap;
    }

    public void updateProfileImage(Uri uri) {
        if(profileImage != null)
            this.profileImage.setImageURI(uri);
    }

    private void setGraphicsComponents(@NotNull View view) {
        checkBoxMostraPassword = view.findViewById(R.id.mostraPswRegistrazione);
        nomeEditText = view.findViewById(R.id.nomeRegistrazione);
        cognomeEditText = view.findViewById(R.id.cognomeRegistrazione);
        usernameEditText = view.findViewById(R.id.usernameRegistrazione);
        passwordEditText = view.findViewById(R.id.passwordRegistrazione);
        confermaPasswordEditText = view.findViewById(R.id.confermaPasswordRegistrazione);
        emailEditText = view.findViewById(R.id.emailRegistrazione);
        registrationButton = view.findViewById(R.id.registratiButton);
        profileImage = view.findViewById(R.id.profileAvatarRegistration);
        pencilToLoadImage = view.findViewById(R.id.editAvatarPencil);

        if(profileImageBitmap != null)
            this.profileImage.setImageBitmap(profileImageBitmap);

        if(nome != null || cognome != null) { //Si nascondono le EditText non necessarie
            passwordEditText.setVisibility(View.INVISIBLE);
            confermaPasswordEditText.setVisibility(View.INVISIBLE);
            emailEditText.setVisibility(View.INVISIBLE);
            checkBoxMostraPassword.setVisibility(View.INVISIBLE);
            nomeEditText.setText(nome);
            cognomeEditText.setText(cognome);
        }
    }
    //endregion

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

    private void addTextChangedListenerToEditTexts() {
        if(afterTextChangedListener != null) {
            nomeEditText.addTextChangedListener(afterTextChangedListener);
            cognomeEditText.addTextChangedListener(afterTextChangedListener);
            usernameEditText.addTextChangedListener(afterTextChangedListener);

            if (nome == null || cognome == null) {
                emailEditText.addTextChangedListener(afterTextChangedListener);
                passwordEditText.addTextChangedListener(afterTextChangedListener);
                confermaPasswordEditText.addTextChangedListener(afterTextChangedListener);
            }
        }
    }

    public String getEmail() {
        if(emailEditText != null)
            return emailEditText.getText().toString().trim();
        return null;
    }

    public String getUsername() {
        if(usernameEditText != null)
            return usernameEditText.getText().toString().trim();
        return null;
    }

    public String getCognome() {
        if(cognomeEditText != null)
            return cognomeEditText.getText().toString().trim();
        return null;
    }

    public String getNome() {
        if(nomeEditText != null)
            return nomeEditText.getText().toString().trim();
        return null;
    }

    public String getPassword() {
        if(passwordEditText != null)
            return passwordEditText.getText().toString().trim();
        return null;
    }

    public String getConfermaPassword() {
        if(confermaPasswordEditText != null)
            return confermaPasswordEditText.getText().toString().trim();
        return null;
    }

    public void enableRegisterButtonIfTextIsNotEmpty() {
        if(registrationButton != null && isAdded() && getActivity() != null)
            getActivity().runOnUiThread(()-> registrationButton.setEnabled(allEditTextAreNotEmpty()));
    }

    public boolean isMostraPasswordChecked() {
        if(checkBoxMostraPassword != null)
            return checkBoxMostraPassword.isChecked();
        return false;
    }

    public void showOrHidePassword(boolean show) {
        if(isAdded() && getActivity() != null)
            getActivity().runOnUiThread(()-> {
                if (show) {
                    if (passwordEditText != null && confermaPasswordEditText != null) {
                        passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        confermaPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }
                } else {
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confermaPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            });
    }

    public void updatePasswordFocus() {
        if(isAdded() && getActivity() != null)
            getActivity().runOnUiThread(()-> {
                if (passwordEditText.hasFocus())
                    passwordEditText.setSelection(passwordEditText.length());
                else if (confermaPasswordEditText.hasFocus())
                    confermaPasswordEditText.setSelection(confermaPasswordEditText.length());
            });
    }
}