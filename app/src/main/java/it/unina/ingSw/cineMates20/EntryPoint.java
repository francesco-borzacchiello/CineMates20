package it.unina.ingSw.cineMates20;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

import it.unina.ingSw.cineMates20.view.login.activity.LoginActivity;
import it.unina.ingSw.cineMates20.view.login.activity.TmpActivity;

public class EntryPoint extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: testare se su uno smartphone vero viene mostrato a schermo questo colore
        this.getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.lightBlue));

        try {
            configureAmplify();
        }catch (Exception e){}

        checkIfUserIsAlreadyLoggedIn();

        finish();
    }

    private void configureAmplify() throws AmplifyException {
        Amplify.addPlugin(new AWSCognitoAuthPlugin());
        Amplify.configure(getApplicationContext());
    }

    private void checkIfUserIsAlreadyLoggedIn() {
        Amplify.Auth.fetchAuthSession(
                isSignIn -> ifIsLoggedInOpenHomeElseOpenLogin(isSignIn.isSignedIn()),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }

    private void ifIsLoggedInOpenHomeElseOpenLogin(boolean isSignedIn) {
        Intent intent;
        if (!isSignedIn)
            //TODO: TmpActivity sarà la Home
            intent = new Intent(EntryPoint.this, TmpActivity.class);
        else
            intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
    }
}
