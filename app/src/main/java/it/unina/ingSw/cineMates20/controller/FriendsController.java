package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.FriendsActivity;
import it.unina.ingSw.cineMates20.view.adapter.FriendsAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class FriendsController {
    //region Attributi
    private static FriendsController instance;
    private FriendsActivity friendsActivity;
    //endregion

    //region Costruttore
    private FriendsController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, FriendsActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //endregion

    //region getInstance() per il pattern singleton
    public static FriendsController getFriendsControllerInstance() {
        if(instance == null)
            instance = new FriendsController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setFriendsActivity(@NotNull FriendsActivity friendsActivity) {
        this.friendsActivity = friendsActivity;
    }
    //endregion

    //TODO: da modificare con utenti reali dopo aver completato applicativo server
    public void initializeActivityFriends() {
        ArrayList<String> nomi = new ArrayList<>(),
                           username = new ArrayList<>(),
                           picturesUrl = new ArrayList<>();

        ArrayList<Runnable> usersLayoutListeners = new ArrayList<>();

        //Popolamento temporaneo con dati fittizzi:
        for(int i = 0; i<30; i++) {
            nomi.add("Nome Cognome");
            username.add("Username");
            //picturesUrl resta per ora null, usiamo foto default
            usersLayoutListeners.add(getUserLayoutListener("Nome Cognome", "Username")); //getUserCardViewListener(utente);
        }

        //if(friends.getCount() > 0) show emptyBoxPicture ... else setAdapter()
        FriendsAdapter friendsAdapter = new FriendsAdapter(friendsActivity, nomi,
                username, picturesUrl, usersLayoutListeners);

        friendsAdapter.setHasStableIds(true);
        friendsActivity.setFriendsRecyclerView(friendsAdapter);
    }

    //TODO: da modificare con utenti reali dopo aver completato applicativo server
    @NotNull
    @Contract(pure = true)
    private Runnable getUserLayoutListener(String nome, String cognome) { //Utente utente
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(friendsActivity)) return;

            //friendsActivity.showFriendsProgressBar(); //Da nascondere poi all'interno dell'activity lanciata
            //Si mostra pagina utente, probabile interazione con uno "UserController"
        };
    }

    //Restituisce un listener le icone della toolbar in FriendsActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            //TODO: spostare negli handler concreti : if(Utilities.checkNullActivityOrNoConnection(homeActivity)) return;

            if(itemId == android.R.id.home)
                friendsActivity.openDrawerLayout();
            //else if(itemId == ...)

            //TODO: aggiungere la gestione degli altri item del menu, come la search (invio richiesta al nostro DB)...
        };
    }

    //Collassa la searchview in caso di annullamento ricerca
    public View.OnFocusChangeListener getSearchViewOnQueryTextFocusChangeListener() {
        return (view, queryTextFocused) -> {
            if (!queryTextFocused) {
                friendsActivity.onResume();
                friendsActivity.setLayoutsForFriends(false);
            }
        };
    }

    public View.OnClickListener getOnSearchClickListener() {
        return (view) -> {
            if(view.getId() == R.id.searchItem)
                friendsActivity.setLayoutsForFriends(true);
        };
    }

    public SearchView.OnQueryTextListener getSearchViewOnQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            //Questo metodo non viene invocato se la query è vuota
            @Override
            public boolean onQueryTextSubmit(String query) {
                //handleOnSearchPressed(query);
                friendsActivity.keepSearchViewExpanded();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }
}
