package it.unina.ingSw.cineMates20;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.core.AmplifyConfiguration;

import it.unina.ingSw.cineMates20.controller.MainController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

/**
 * Fa da punto d'ingresso all'applicativo.
 * Inizializza dei componenti che saranno utilizzati nell'applicativo.
 * Chiama il controller principale che deciderà quale activity mostrare tra la login e la home.
 * Si utilizza un activity come punto d'ingresso poichè android come primo componente avvia un'activity
 */
public class EntryPoint extends AppCompatActivity {

    //region oonCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity();

        MainController mainController = new MainController(this);
        if(Utilities.isOnline(getApplicationContext()))
            mainController.start();
        else {
            Utilities.stampaToast(this, getApplicationContext().getResources().getString(R.string.networkNotAvailable));
            finish();
        }
    }
    //endregion

    //region Inizializza l'activity che fa da entry point per l'applicativo
    private void initActivity() {
        setContentView(R.layout.activity_entry_point);

        try {
            configureAmplify();
        }catch (AmplifyException e){
            Log.e("initActivityException", "Errore configurazione Amplify: " + e.getLocalizedMessage());
        }
    }
    //endregion

    //region Configurazione di amplify, per poter interagire con Cognito nell'applicativo
    private void configureAmplify() throws AmplifyException {
        Amplify.addPlugin(new AWSCognitoAuthPlugin());

        AmplifyConfiguration config = AmplifyConfiguration.builder(getApplicationContext()).devMenuEnabled(false).build();
        Amplify.configure(config, getApplicationContext());
    }
    //endregion
}