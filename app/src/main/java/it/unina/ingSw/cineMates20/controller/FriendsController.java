package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.FriendsActivity;
import it.unina.ingSw.cineMates20.view.adapter.FriendsAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class FriendsController {
    //region Attributi
    private static FriendsController instance;
    private FriendsActivity friendsActivity;
    private FriendsAdapter friendsAdapter;
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
    public void initializeActivityFriendsAdapter() {
        ArrayList<UserDB> users = new ArrayList<>();

        ArrayList<Runnable> usersLayoutListeners = new ArrayList<>();

        //Popolamento temporaneo con dati fittizzi:
        for(int i = 0; i<20; i++) {
            UserDB user = new UserDB("Username", "Nome", "Cognome", "email@gmail.com", "utente");
            users.add(user);
            usersLayoutListeners.add(getUserLayoutListener(user));
        }

        if(users.size() == 0)
            showEmptyFriendsLayout(true);
        else {
            friendsAdapter = new FriendsAdapter(friendsActivity, users, usersLayoutListeners);

            friendsAdapter.setHasStableIds(true);
            friendsActivity.setFriendsRecyclerView(friendsAdapter);
        }
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getUserLayoutListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(friendsActivity)) return;

            friendsActivity.showFriendsProgressBar(true);
            UserController.getUserControllerInstance().start(friendsActivity, user);
        };
    }

    public void hideFriendsActivityProgressBar() {
        if(friendsActivity != null)
            friendsActivity.showFriendsProgressBar(false);
    }

    //Restituisce un listener le icone della toolbar in FriendsActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(itemId == android.R.id.home)
                friendsActivity.openDrawerLayout();
            else if(itemId == R.id.notificationItem &&
                    !Utilities.checkNullActivityOrNoConnection(friendsActivity)) {
                NotificationController.getNotificationControllerInstance().start(friendsActivity);
            }
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
                handleOnSearchPressed(query);
                friendsActivity.keepSearchViewExpanded();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    private void handleOnSearchPressed(String query) {
        if(Utilities.checkNullActivityOrNoConnection(friendsActivity)) return;

        SearchFriendsController.getSearchFriendsControllerInstance().start(friendsActivity, query);
    }

    public void removeSelectedFriend() {
        if(friendsAdapter != null)
            friendsAdapter.deleteLastClickedItem();
    }

    public void showEmptyFriendsLayout(boolean show) {
        if(friendsActivity != null)
            friendsActivity.showEmptyFriendsLayout(show);
    }
}
