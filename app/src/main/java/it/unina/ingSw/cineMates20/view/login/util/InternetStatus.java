package it.unina.ingSw.cineMates20.view.login.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * La seguente classe è utilizzata per determinare se l'applicazione dispone di una connessione
 * Mobile o WIFI, prima di inviare qualunque richiesta internet al server.
 * La classe utilizza due permessi - INTERNET e ACCESS NETWORK STATE, per determinare lo
 * stato di connessione dell'utente.
 * Esempio di utilizzo all'interno di un'activity: InternetStatus.getInstance(getApplicationContext()).isOnline()
 */

public class InternetStatus {

    static Context context;
    private static InternetStatus instance = new InternetStatus();
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static InternetStatus getInstance(Context ctx) {
        if(ctx != null) {
            context = ctx.getApplicationContext();
            return instance;
        }
        else
            return null;
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            Log.v("connectivity", e.toString());
        }
        return connected;
    }
}
