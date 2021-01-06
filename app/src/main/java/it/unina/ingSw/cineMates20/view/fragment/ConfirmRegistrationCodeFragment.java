package it.unina.ingSw.cineMates20.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;

public class ConfirmRegistrationCodeFragment extends Fragment {

    public ConfirmRegistrationCodeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(getContext())
                        .setMessage("Sei sicuro di voler annullare?")
                        .setCancelable(false)
                        .setPositiveButton("Si", (dialog, id) -> getActivity().getSupportFragmentManager().popBackStack())
                        .setNegativeButton("No", null)
                        .show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_registration_code, container, false);
    }
}