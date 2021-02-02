package it.unina.ingSw.cineMates20.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import info.movito.themoviedbapi.model.MovieDb;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.ReportController;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ReportActivity extends AppCompatActivity {
    private ReportController reportController;
    private EditText otherReasonEditText;
    private Button reportButton;
    private RadioGroup radioGroup;
    private boolean reportSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        reportController = ReportController.getReportControllerInstance();
        reportController.setReportActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        if(reportController.isUserReport()) {
            setContentView(R.layout.activity_report_user);
            initializeReportedUser();
        }
        else {
            setContentView(R.layout.activity_report_movie);
            initializeReportedMovie();
        }
        otherReasonEditText = findViewById(R.id.otherReasonReportEditText);
        otherReasonEditText.addTextChangedListener(reportController.getOtherReasonEditTextTextWatcher());

        radioGroup = findViewById(R.id.reportRadioGroup);

        setReportToolbar();
        initializeRadioButtons();
    }

    private void initializeReportedMovie() {
        MovieDb movie = reportController.getReportedMovie();
        TextView title = findViewById(R.id.title_movie_report);
        TextView description = findViewById(R.id.description_movie_report);
        ImageView poster = findViewById(R.id.cover_movie_report);

        if(movie.getTitle() != null)
            title.setText(movie.getTitle());
        else
            title.setText(movie.getOriginalTitle());

        description.setText(movie.getOverview());

        if(movie.getPosterPath() != null) {
            String firstPath = getResources().getString(R.string.first_path_image);
            Picasso.get().load(firstPath + movie.getPosterPath()).resize(270, 360).
                    noFade().into(poster,
                    new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError(Exception e) {}
                    });
        }
    }

    private void initializeReportedUser() {
        UserDB user = reportController.getReportedUser();
        TextView nameTextView = findViewById(R.id.reportedName);
        nameTextView.setSelected(true);
        TextView emailTextView = findViewById(R.id.reportedEmail);
        emailTextView.setSelected(true);
        TextView usernameTextView = findViewById(R.id.reportedUsername);
        usernameTextView.setSelected(true);
        ImageView profilePicture = findViewById(R.id.reportedUserProfilePicture);

        String fullName = user.getNome() + " " + user.getCognome();
        nameTextView.setText(fullName);
        emailTextView.setText(user.getEmail());
        String username = "@" + user.getUsername();
        usernameTextView.setText(username);

        String profilePictureUrl = reportController.getUserToReportProfilePictureUrl();
        if(profilePictureUrl != null)
            Picasso.get().load(profilePictureUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).resize(95, 95).noFade()
                .into(profilePicture,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                profilePicture.setAlpha(0f);
                                profilePicture.animate().setDuration(100).alpha(1f).start();
                            }

                            @Override
                            public void onError(Exception e) {}
                        });
    }

    private void initializeRadioButtons() {
        RadioGroup reportRadioGroup = findViewById(R.id.reportRadioGroup);
        reportRadioGroup.setOnCheckedChangeListener(reportController.getRadioGroupOnCheckedChangeListener());

        reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener(reportController.getReportButtonOnClickListener());
    }

    private void setReportToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

            if(reportController.isUserReport())
                ab.setTitle("Segnala utente");
            else
                ab.setTitle("Segnala film");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        menu.findItem(R.id.searchItem).setVisible(false);
        menu.findItem(R.id.notificationItem).setVisible(false);
        menu.findItem(R.id.shareItem).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = reportController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(this, "Si è verificato un errore");
        }

        return super.onOptionsItemSelected(item);
    }

    public void showOtherReasonEditText(boolean show) {
        if(reportSent) return;

        runOnUiThread(()-> {
            if(show) {
                otherReasonEditText.setVisibility(View.VISIBLE);
                if(otherReasonEditText.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(otherReasonEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
            else
                otherReasonEditText.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }

    public void enableReportButton(boolean enable) {
        if(!reportSent)
            runOnUiThread(()-> reportButton.setEnabled(enable));
    }

    public boolean otherReasonEditTextIsEmpty() {
        return otherReasonEditText.getText().toString().trim().length() == 0;
    }

    public void setReportSent(boolean sent) {
        reportSent = sent;
    }

    public void disableReportComponents() {
        runOnUiThread(()-> {
            otherReasonEditText.setEnabled(false);
            RadioButton button1 = findViewById(R.id.reportRadioButton1);
            RadioButton button2 = findViewById(R.id.reportRadioButton2);
            RadioButton button3 = findViewById(R.id.reportRadioButton3);
            RadioButton button4 = findViewById(R.id.otherReasonReportRadioButton);
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setEnabled(false);
            button4.setEnabled(false);
        });
    }

    public String getReportedMessage() {
        RadioButton selectedRadioButton = findViewById(radioGroup.getCheckedRadioButtonId());

        if(selectedRadioButton.getText().toString().equals(getResources().getString(R.string.otherReasonReport)))
            return otherReasonEditText.getText().toString();

        return selectedRadioButton.getText().toString();
    }
}