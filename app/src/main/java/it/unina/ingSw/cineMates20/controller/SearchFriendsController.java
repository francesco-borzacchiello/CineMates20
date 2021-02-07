package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.model.UserHttpRequests;
import it.unina.ingSw.cineMates20.view.activity.SearchFriendsActivity;
import it.unina.ingSw.cineMates20.view.adapter.FriendsAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchFriendsController {
    private static SearchFriendsController instance;
    private SearchFriendsActivity searchFriendsActivity;
    private AmazonCognitoIdentityProviderClient identityProviderClient;
    private static final String AWS_ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY,
                                AWS_SECRET_KEY = BuildConfig.AWS_SECRET_KEY;

    private SearchFriendsController() {
        AtomicBoolean done = new AtomicBoolean(false);
        Thread t = new Thread(()-> {
            identityProviderClient = new AmazonCognitoIdentityProviderClient(new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() { return AWS_ACCESS_KEY; }

                @Override
                public String getAWSSecretKey() { return AWS_SECRET_KEY; }
            });
            identityProviderClient.setRegion(Region.getRegion(Regions.EU_WEST_3));

            done.set(true);

            synchronized (done) {
                done.notifyAll();
            }
        });
        t.start();

        while(!done.get()){
            synchronized(done){
                try {
                    done.wait();
                }catch (InterruptedException ignore){}
            }
        }
    }

    public static SearchFriendsController getSearchFriendsControllerInstance() {
        if(instance == null)
            instance = new SearchFriendsController();
        return instance;
    }

    public void start(Activity parent, String query) {
        Intent intent = new Intent(parent, SearchFriendsActivity.class);
        intent.putExtra("friendsSearchText", query);
        parent.startActivity(intent);
        parent.overridePendingTransition(0,0);
    }

    public void setSearchFriendsActivity(SearchFriendsActivity searchFriendsActivity) {
        this.searchFriendsActivity = searchFriendsActivity;
    }

    public boolean initializeFriendsSearch(String query) {
        searchFriendsActivity.clearSearchViewFocus();
        searchFriendsActivity.setSearchText(query);
        return initializeAdapterForFriendsSearch(query);
    }

    private boolean initializeAdapterForFriendsSearch(String query) {
        ArrayList<UserDB> users = new ArrayList<>(UserHttpRequests.getInstance().getUsersByQuery(query));
        users.remove(User.getLoggedUser(searchFriendsActivity));

        if(users.size() > 0) {
            ArrayList<Runnable> usersLayoutListeners = new ArrayList<>();

            for(UserDB user: users)
                usersLayoutListeners.add(getUserLayoutListener(user));

            FriendsAdapter actualSearchFriendsAdapter = new FriendsAdapter(searchFriendsActivity, users, usersLayoutListeners);
            actualSearchFriendsAdapter.setHasStableIds(true);
            searchFriendsActivity.setFriendsRecyclerView(actualSearchFriendsAdapter);
        }

        searchFriendsActivity.showProgressBar(false);
        return users.size() > 0;
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getUserLayoutListener(UserDB user) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(searchFriendsActivity)) return;

            searchFriendsActivity.showProgressBar(true);
            UserController.getUserControllerInstance().start(searchFriendsActivity, user);
        };
    }

    public MenuItem.OnActionExpandListener getSearchViewOnActionExpandListener() {
        return new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchFriendsActivity.finish(); //Termina la corrente sessione di ricerca (@Override finish())
                return false;
            }
        };
    }

    public SearchView.OnQueryTextListener getSearchViewOnQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(Utilities.checkNullActivityOrNoConnection(searchFriendsActivity)) {
                    searchFriendsActivity.clearSearchViewFocus();
                    return false;
                }

                searchFriendsActivity.showNextSearchFragment(!initializeFriendsSearch(query));
                searchFriendsActivity.updateSearchQueue(query);
                searchFriendsActivity.showProgressBar(false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        };
    }

    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(searchFriendsActivity)) return;

            if(itemId == R.id.notificationItem)
                NotificationController.getNotificationControllerInstance().start(searchFriendsActivity);
        };
    }

    public void hideProgressBar() {
        if(searchFriendsActivity != null)
            searchFriendsActivity.showProgressBar(false);
    }
}
