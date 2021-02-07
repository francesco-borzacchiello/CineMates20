package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.SearchFriendsController;
import it.unina.ingSw.cineMates20.controller.SettingsController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.fragment.EmptySearchFragment;
import it.unina.ingSw.cineMates20.view.fragment.NotEmptyFriendsSearchFragment;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchFriendsActivity extends AppCompatActivity {
    private SearchFriendsController searchFriendsController;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private NotEmptyFriendsSearchFragment notEmptyFriendsSearchFragment;
    private EmptySearchFragment emptySearchFragment;
    private FragmentManager manager;
    private String searchText;
    private SearchView searchView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> currentRecyclerViewAdapter;
    private LinkedList<String> searchHistory;
    private ProgressBar progressBar;
    private ImageView fotoProfilo;
    private MenuItem notificationItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friends);

        initializeGraphicsComponents();

        Bundle srcTxtBundle = getIntent().getExtras();
        if(srcTxtBundle != null) {
            searchText = srcTxtBundle.getString("friendsSearchText");
            searchHistory = new LinkedList<>();
            searchHistory.offer(searchText);
        }
        else {
            finish();
            return;
        }

        searchFriendsController = SearchFriendsController.getSearchFriendsControllerInstance();
        searchFriendsController.setSearchFriendsActivity(this);
        HomeController.getHomeControllerInstance().setSearchFriendsActivity(this);

        //Inizializzo l'unica istanza di FragmentSearchEmpty necessaria
        emptySearchFragment = new EmptySearchFragment();

        configureNavigationDrawer();
    }

    private void initializeGraphicsComponents() {
        drawerLayout = findViewById(R.id.searchFriendsNavMenuDrawerLayout);
        progressBar = findViewById(R.id.progressBarSearchFriends);
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setTitle("");
        }
    }

    private void configureNavigationDrawer() {
        navigationView = findViewById(R.id.navigationViewSearchFriends);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);
        fotoProfilo = navigationView.getHeaderView(0).findViewById(R.id.imageProfile);

        runOnUiThread(() -> {
            UserDB user = User.getLoggedUser(this);
            nomeTextView.setText(user.getNome());
            cognomeTextView.setText(user.getCognome());

            String profilePictureUrl = User.getUserProfilePictureUrl();
            if(profilePictureUrl != null)
                refreshProfilePicture(profilePictureUrl);
        });
    }

    private void refreshProfilePicture(String imageUrl) {
        Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).resize(75, 75).noFade()
                .into(fotoProfilo,
                        new Callback() {
                            @Override
                            public void onSuccess() {}

                            @Override
                            public void onError(Exception e) {}
                        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        //Inizializza la searchView con un hint
        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un utente");

        //La ricerca ha prodotto risultati
        if(searchFriendsController.initializeFriendsSearch(searchText)) {
            notEmptyFriendsSearchFragment = new NotEmptyFriendsSearchFragment(currentRecyclerViewAdapter);
            createFirstFragment(notEmptyFriendsSearchFragment);
        }
        else
            createFirstFragment(emptySearchFragment);

        //Espandi la searchView ed elimina il focus
        menu.findItem(R.id.searchItem).expandActionView();
        searchView.setQuery(searchText, false);
        searchView.clearFocus();

        //Inizializza listener searchView
        menu.findItem(R.id.searchItem).setOnActionExpandListener(searchFriendsController.getSearchViewOnActionExpandListener());

        //Inizializza listener query di searchView
        searchView.setOnQueryTextListener(searchFriendsController.getSearchViewOnQueryTextListener());

        setNavigationViewActionListener();

        notificationItem = menu.findItem(R.id.notificationItem);

        if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled()) {
            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
            scheduleTaskExecutor.scheduleAtFixedRate(this::setUpNotificationIcon, 0, 15, TimeUnit.SECONDS);
        }

        return true;
    }

    //Nasconde la tastiera alla pressione di un elemento che non sia essa stessa o una text box
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }

    private void setUpNotificationIcon() {
        new Thread(()-> runOnUiThread(()-> {
            if(User.getTotalUserNotificationCount() > 0)
                notificationItem.setIcon(R.drawable.ic_notifications_on);
            else
                notificationItem.setIcon(R.drawable.ic_notifications);
        })).start();
    }

    private void setNavigationViewActionListener() {
        //Listener per icone del NavigationView
        navigationView.setNavigationItemSelectedListener(
            item -> {
                Runnable r = HomeController.getHomeControllerInstance().
                        getNavigationViewOnOptionsItemSelected(this, item.getItemId());
                try {
                    r.run();
                }catch(NullPointerException e){
                    Utilities.stampaToast(SearchFriendsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                }
                return false;
            }
        );
    }

    //Inizializza per la prima volta il fragment dell'activity
    private void createFirstFragment(Fragment fragment) {
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.showFriendsFrameLayout, fragment);
        transaction.commit();
    }

    public void showNextSearchFragment(boolean isEmptySearch) {
        if(!isEmptySearch) {
            if(manager.getBackStackEntryCount() > 3) {
                for(int i = 0; i < manager.getBackStackEntryCount(); ++i) {
                    manager.popBackStack();
                }
                String head = searchHistory.getFirst();
                searchHistory.clear();
                searchHistory.add(head);
            }

            FragmentTransaction transaction = manager.beginTransaction();

            notEmptyFriendsSearchFragment = new NotEmptyFriendsSearchFragment(currentRecyclerViewAdapter);

            transaction.replace(R.id.showFriendsFrameLayout, notEmptyFriendsSearchFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(!emptySearchFragment.isVisible()) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.showFriendsFrameLayout, emptySearchFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = searchFriendsController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(SearchFriendsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void clearSearchViewFocus() {
        runOnUiThread(()-> {
            searchView.setFocusable(false);
            searchView.setIconified(false);
            searchView.clearFocus();
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        String profilePicUrl = User.getUserProfilePictureUrl();
        if(profilePicUrl != null)
            refreshProfilePicture(profilePicUrl);
    }

    @Override
    public void finish() {
        if (manager.getBackStackEntryCount() == 0) {
            super.finish();
            overridePendingTransition(0,0);
        }
        else {
            if(searchHistory.size() == 1) {
                String head = searchHistory.pollLast();
                manager.popBackStack();
                searchView.setQuery(head, false);
            }
            else {
                searchHistory.removeLast();
                manager.popBackStack();
                searchView.setQuery(searchHistory.getLast(), false);
            }
        }
    }

    public void setSearchText(String query) {
        if(query != null && !query.equals(""))
            searchText = query;
    }

    public void setFriendsRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        currentRecyclerViewAdapter = adapter;
    }

    public void updateSearchQueue(String query) {
        searchHistory.offer(query);
    }

    public void showProgressBar(boolean show) {
        runOnUiThread(()-> {
            if(show)
                progressBar.setVisibility(View.VISIBLE);
            else
                progressBar.setVisibility(View.INVISIBLE);
        });
    }

    public void closeDrawerLayout() {
        runOnUiThread(()-> drawerLayout.closeDrawer(GravityCompat.START));
    }

}