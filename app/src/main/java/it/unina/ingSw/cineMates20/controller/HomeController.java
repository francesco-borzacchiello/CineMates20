package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;

import com.amplifyframework.core.Amplify;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class HomeController {

    //region Attributi
    private static HomeController instance;
    private HomeActivity homeActivity;
    //endregion

    //region Costruttore
    private HomeController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
    }
    //endregion

    //region getIstance() per il pattern singleton
    public static HomeController getHomeControllerInstance() {
        if(instance == null)
            instance = new HomeController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setHomeActivity(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }
    //endregion

    public HomeActivity getHomeActivity() {
        return homeActivity;
    }

    //Restituisce un listener per una delle icone della toolbar
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            //TODO: spostare negli handler concreti : if(Utilities.checkNullActivityOrNoConnection(homeActivity)) return;

            //Nota: non è possibile fare switch case a causa del fatto che le risorse in id non sono più final
            if(itemId == android.R.id.home)
                homeActivity.getDrawerLayout().openDrawer(GravityCompat.START);
            //else if(itemId == ...)

            //TODO: aggiungere la gestione degli altri item del menu, come la search (invio richiesta a themoviedb)...
        };
    }

    public SearchView.OnQueryTextListener getSearchViewOnQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            //Questo metodo non viene invocato se la query è vuota
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleOnSearchPressed(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    private void handleOnSearchPressed(String query) {
        if(Utilities.checkNullActivityOrNoConnection(homeActivity)) return;

        SearchMovieController.getSearchMovieControllerInstance().start(homeActivity, query);
    }

    //Restituisce un listener per una delle icone del NavigationView
    public Runnable getNavigationViewOnOptionsItemSelected(Activity activity, int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(activity)) return;

            if(itemId == R.id.menuLogout)
                handleLogoutMenuItem(activity);
            else if(itemId == R.id.menuHome)
                handleHomeMenuItem(activity);

            //TODO: aggiungere la gestione degli altri item del menu...
        };
    }

    private void handleHomeMenuItem(@NotNull Activity activity) {
        if(activity.equals(homeActivity))
            homeActivity.getDrawerLayout().closeDrawer(GravityCompat.START);
        else {
            Intent intent = new Intent(activity, HomeActivity.class);
            activity.runOnUiThread(() -> Utilities.resumeBottomBackStackActivity(intent));

            activity.startActivity(intent);
            activity.overridePendingTransition(0,0);
            activity.finish();
        }
    }

    //region Gestore d'evento per il logout
    private void handleLogoutMenuItem(Activity activity) {
        if(activity != null)
            //Chiede all'utente se è sicuro di volersi disconnettersi (logout), in caso positivo si torna alla login
            new AlertDialog.Builder(activity)
                    .setMessage("Sei sicuro di volerti disconnettere?")
                    .setCancelable(false)
                    .setPositiveButton("Si", (dialog, id) -> {
                        if (Utilities.checkNullActivityOrNoConnection(activity)) return;
                        logOut();
                    })
                    .setNegativeButton("No", null)
                    .show();
    }

    //region Logica del logout
    private void logOut() {
        Amplify.Auth.signOut(
                () -> {
                    //Mostra schermata home con un intent, passando inizialmente homeActivity come parent e poi distruggendo tutte le activity create
                    Intent intent = new Intent(homeActivity, EntryPoint.class);
                    homeActivity.runOnUiThread(() -> Utilities.clearBackStack(intent));
                    homeActivity.startActivity(intent);
                    homeActivity.finish();
                    homeActivity.runOnUiThread(() -> Utilities.stampaToast(homeActivity, "Logout effettuato."));
                },
                error -> homeActivity.runOnUiThread(() -> Utilities.stampaToast(homeActivity, "Si è verificato un errore.\nRiprova tra qualche minuto."))
        );
    }

    //endregion
    //endregion
}
