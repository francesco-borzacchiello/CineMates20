package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidentityprovider.model.AttributeType;
import com.amazonaws.services.cognitoidentityprovider.model.ListUsersRequest;
import com.amazonaws.services.cognitoidentityprovider.model.ListUsersResult;
import com.amazonaws.services.cognitoidentityprovider.model.UserType;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.SearchFriendsActivity;
import it.unina.ingSw.cineMates20.view.adapter.FriendsAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchFriendsController {
    private static SearchFriendsController instance;
    private SearchFriendsActivity searchFriendsActivity;
    private AmazonCognitoIdentityProviderClient identityProviderClient;
    private FriendsAdapter actualSearchFriendsAdapter;

    private SearchFriendsController() {
        AtomicBoolean done = new AtomicBoolean(false);
        Thread t = new Thread(()-> {
            identityProviderClient = new AmazonCognitoIdentityProviderClient(new AWSCredentials() {
                //TODO: Spostare queste credenziali in un luogo sicuro
                @Override
                public String getAWSAccessKeyId() {
                    return "AKIASIIR74FZGEJFLJGN";
                }

                @Override
                public String getAWSSecretKey() {
                    return "S4hzx3zzCLBEGb8OAbu7TkyC176wdoA+KI/8aRll";
                }
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

    //TODO: aggiungere utenti presenti nel nostro database (utenti social)
    private boolean initializeAdapterForFriendsSearch(String query) {
        boolean[] ret = new boolean[1];

        Thread t = new Thread(()-> {
            LinkedHashSet<UserDB> usersSet = new LinkedHashSet<>(); //Non occorre controllare risultati duplicati
            ArrayList<Runnable> usersLayoutListeners = new ArrayList<>();
            List<ListUsersResult> results = getDefaultResults(query);
            String loggedUserEmail = User.getLoggedUser(searchFriendsActivity).getEmail();
            boolean validUser = false;

            for(ListUsersResult result: results) { //Itera sui 4 tipi di risultati (Nome, Cognome, Username, Email)
                for (UserType userType : result.getUsers()) { //Itera sugli utenti trovati per ogni categoria di risultati
                    String name = "", surname = "", username = "", email = "";
                    for (AttributeType attribute : userType.getAttributes()) { //Itera sugli attributi dell'utente trovato
                        switch(attribute.getName()) {
                            case "email_verified": {
                                if (attribute.getValue().equals("true"))
                                    validUser = true; //L'account è ben formato
                                break;
                            }
                            case "given_name": {
                                name = attribute.getValue();
                                break;
                            }
                            case "family_name": {
                                surname = attribute.getValue();
                                break;
                            }
                            case "email": {
                                email = attribute.getValue();
                                break;
                            }
                            case "preferred_username": {
                                username = attribute.getValue();
                                break;
                            }
                        }
                    }
                    if(validUser) {
                        UserDB user = new UserDB(username, name, surname, email, "utente");
                        if(!user.getEmail().equals(loggedUserEmail) && usersSet.add(user))
                            usersLayoutListeners.add(getUserLayoutListener(user));
                        validUser = false;
                    }
                }
            }

            ret[0] = usersSet.size() > 0;

            if(usersSet.size() > 0) {
                actualSearchFriendsAdapter = new FriendsAdapter(searchFriendsActivity, new ArrayList<>(usersSet), usersLayoutListeners);
                actualSearchFriendsAdapter.setHasStableIds(true);
                searchFriendsActivity.setFriendsRecyclerView(actualSearchFriendsAdapter);
            }

            searchFriendsActivity.showProgressBar(false);
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return ret[0];
    }

    @NotNull
    private List<ListUsersResult> getDefaultResults(String query) {
        List<ListUsersResult> results = new LinkedList<>();

        ListUsersRequest listUsersGivenName = new ListUsersRequest();
        listUsersGivenName.withUserPoolId("eu-west-3_VN56xO6X5");
        listUsersGivenName.withFilter("given_name ^= \"" + query + "\"");

        ListUsersRequest listUsersFamilyName = new ListUsersRequest();
        listUsersFamilyName.withUserPoolId("eu-west-3_VN56xO6X5");
        listUsersFamilyName.withFilter("family_name ^= \"" + query + "\"");

        ListUsersRequest listUsersUsername = new ListUsersRequest();
        listUsersUsername.withUserPoolId("eu-west-3_VN56xO6X5");
        listUsersUsername.withFilter("preferred_username ^= \"" + query + "\"");

        ListUsersRequest listUsersEmail = new ListUsersRequest();
        listUsersEmail.withUserPoolId("eu-west-3_VN56xO6X5");
        listUsersEmail.withFilter("email ^= \"" + query + "\"");

        results.add(identityProviderClient.listUsers(listUsersGivenName));
        results.add(identityProviderClient.listUsers(listUsersFamilyName));
        results.add(identityProviderClient.listUsers(listUsersUsername));
        results.add(identityProviderClient.listUsers(listUsersEmail));

        return results;
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
