package it.unina.ingSw.cineMates20.view.fragment;

import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;

public class ConfirmRegistrationCodeFragment extends Fragment {
    private EditText confermaCodice;
    private Button inviaCodice;
    private Button reinviaCodice;
    private TextWatcher confermaCodiceTextChangedListener;
    private View.OnClickListener inviaCodiceOnClickListener;
    private View.OnClickListener reinviaCodiceOnClickListener;

    public ConfirmRegistrationCodeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_registration_code, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        initializeGraphicComponents(view);

        setAllActionListener();
    }

    public void setTextWatcherConfermaCodice(TextWatcher txtw) {
        confermaCodiceTextChangedListener = txtw;
    }

    public void setInviaCodiceOnClickListener(View.OnClickListener listener) {
        inviaCodiceOnClickListener = listener;
    }

    public void setReinviaCodiceOnClickListener(View.OnClickListener listener) {
        reinviaCodiceOnClickListener = listener;
    }

    private void setAllActionListener() {
        if(confermaCodiceTextChangedListener != null) {
            confermaCodice.addTextChangedListener(confermaCodiceTextChangedListener);
        }
        if(inviaCodiceOnClickListener != null) {
            inviaCodice.setOnClickListener(inviaCodiceOnClickListener);
        }
        if(reinviaCodiceOnClickListener != null) {
            reinviaCodice.setOnClickListener(reinviaCodiceOnClickListener);
        }
    }

    private void initializeGraphicComponents(@NotNull View view) {
        confermaCodice = view.findViewById(R.id.confermaCodiceRegistrazioneEditText);
        inviaCodice = view.findViewById(R.id.inviaCodiceRegistrazioneButton);
        reinviaCodice = view.findViewById(R.id.reinviaCodiceButton);
    }

    public void setEnableSendButton(boolean enable) {
        if(inviaCodice != null && isAdded() && getActivity() != null)
            getActivity().runOnUiThread(()-> inviaCodice.setEnabled(enable));
    }

    public int getLengthEditTextInviaCodice() {
        return confermaCodice.getText().toString().trim().length();
    }

    public String getCodiceDiConferma() {
        if(confermaCodice != null)
            return confermaCodice.getText().toString().trim();
        return null;
    }
}