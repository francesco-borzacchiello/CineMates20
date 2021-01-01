package it.unina.ingSw.cineMates20;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.controller.MainController;
import it.unina.ingSw.cineMates20.view.util.InternetStatus;

public class EntryPoint extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity();

        MainController x = new MainController(this);
        x.start();

        finish();
    }

    private void configureAmplify() throws AmplifyException {
        Amplify.addPlugin(new AWSCognitoAuthPlugin());
        Amplify.configure(getApplicationContext());
    }

    private void initActivity() {
        //TODO: testare se su uno smartphone vero viene mostrato a schermo questo colore
        this.getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.lightBlue));

        //Inizializziamo l'istanza singleton di InternetStatus (non necessitiamo del riferimento)
        InternetStatus.getInstance();

        try {
            configureAmplify();
        }catch (Exception e){
            Log.e("initActivityException", "Errore configurazione Amplify");
        }
    }
}