package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.RadioGroup;

import info.movito.themoviedbapi.model.MovieDb;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.ReportActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class ReportController {
    private static ReportController instance;
    private ReportActivity reportActivity;
    private MovieDb movie;
    private UserDB user;

    private ReportController() {}

    public static ReportController getReportControllerInstance() {
        if(instance == null)
            instance = new ReportController();
        return instance;
    }

    public void setReportActivity(ReportActivity reportActivity) {
        this.reportActivity = reportActivity;
    }

    public void startMovieReport(Activity activity, MovieDb movie) {
        Intent intent = new Intent(activity, ReportActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        this.movie = movie;
        this.user = null;
    }

    public void startUserReport(Activity activity, UserDB user) {
        Intent intent = new Intent(activity, ReportActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        this.user = user;
        this.movie = null;
    }

    //Restituisce un listener le icone della toolbar in ReportActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(itemId == android.R.id.home) {
                reportActivity.finish();
                reportActivity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
    }

    public boolean isUserReport() {
        return user != null;
    }

    public MovieDb getReportedMovie() {

        return movie;
    }

    public UserDB getReportedUser() {
        return user;
    }

    public TextWatcher getOtherReasonEditTextTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s,int start, int count, int after){}

            @Override
            public void onTextChanged (CharSequence s,int start, int before, int count){}

            @Override
            public void afterTextChanged (Editable s){
                if(reportActivity == null) return;

                reportActivity.enableReportButton(!reportActivity.otherReasonEditTextIsEmpty());
            }
        };
    }

    public View.OnClickListener getReportButtonOnClickListener() {
        return v -> {
            //Alla pressione di "Segnala" viene inviata la segnalazione e disabilitato il pulsante
            report();
            reportActivity.enableReportButton(false);
            reportActivity.setReportSent(true);
            reportActivity.disableReportComponents();
        };
    }

    public RadioGroup.OnCheckedChangeListener getRadioGroupOnCheckedChangeListener() {
        return (group, checkedId) -> {
            if(checkedId == R.id.otherReasonReportRadioButton) {
                reportActivity.showOtherReasonEditText(true);
                if(reportActivity.otherReasonEditTextIsEmpty())
                    reportActivity.enableReportButton(false);
            }
            else {
                reportActivity.showOtherReasonEditText(false);
                reportActivity.enableReportButton(true);
            }
        };
    }

    //TODO: inviare segnalazione al database interno
    public void report() {
        Utilities.stampaToast(reportActivity, "Segnalazione inviata");
    }
}
