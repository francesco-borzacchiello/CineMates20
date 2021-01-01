package it.unina.ingSw.cineMates20.view.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * La seguente classe è utilizzata come singleton per determinare se l'applicazione dispone di una
 * connessione Mobile o WIFI, prima di inviare qualunque richiesta internet al server.
 * La classe utilizza due permessi - INTERNET e ACCESS NETWORK STATE, per determinare lo
 * stato di connessione dell'utente.
 */

public class InternetStatus {

    private static InternetStatus instance;
    private ConnectivityManager connectivityManager;
    boolean connected = false;

    /**
     * Questo costruttore necessita di essere chiamato soltanto una volta.
     */
    private InternetStatus() {}

    public static InternetStatus getInstance() {
        if(instance == null)
            instance = new InternetStatus();
        return instance;
    }

    public boolean isOnline(Context context) {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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

/* VECCHIO CODICE TEMPORANEO
public class InternetStatus {

    static Context context; //Problema leak di memoria
    private static final InternetStatus instance = new InternetStatus();
    ConnectivityManager connectivityManager;
    boolean connected = false;

    private InternetStatus() {}

    public static void initializeContext(Context ctx) {
        if(ctx != null)
            context = ctx.getApplicationContext();
    }

    public static InternetStatus getInstance() {
        return instance;
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

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
*/
