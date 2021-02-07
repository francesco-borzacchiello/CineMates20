package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.FriendsController;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.SettingsController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class FriendsActivity extends AppCompatActivity {
    private FriendsController friendsController;
    private NavigationView navigationView;
    private DrawerLayout friendsDrawerLayout;
    private SearchView searchView;
    private Menu menu;
    private ProgressBar progressBar;
    private RecyclerView friendsRecyclerView;
    private TextView emptyFriendsListTextView;
    private LinearLayout friendsLinearLayout;
    private ImageView profilePicture;
    private MenuItem notificationItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendsController = FriendsController.getFriendsControllerInstance();
        friendsController.setFriendsActivity(this);
        HomeController.getHomeControllerInstance().setFriendsActivity(this);

        initializeGraphicsComponents();
        configureNavigationDrawer();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(friendsDrawerLayout.isOpen())
                    closeDrawerLayout();
                else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_friends);
        setToolbar();

        progressBar = findViewById(R.id.progressBarFriends);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        emptyFriendsListTextView = findViewById(R.id.emptyFriendsListTextView);
        friendsLinearLayout = findViewById(R.id.friendsLinearLayout);

        friendsController.initializeActivityFriendsAdapter();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Amici");
        }
    }

    private void configureNavigationDrawer() {
        friendsDrawerLayout = findViewById(R.id.friendsDrawerLayout);
        navigationView = findViewById(R.id.friendsNavigationView);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);
        profilePicture = navigationView.getHeaderView(0).findViewById(R.id.imageProfile);

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
                .into(profilePicture,
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

        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un utente");

        searchView.setOnQueryTextListener(friendsController.getSearchViewOnQueryTextListener());
        searchView.setOnQueryTextFocusChangeListener(friendsController.getSearchViewOnQueryTextFocusChangeListener());
        searchView.setOnSearchClickListener(friendsController.getOnSearchClickListener());
        notificationItem = menu.findItem(R.id.notificationItem);

        setNavigationViewActionListener();
        this.menu = menu;

        if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled()) {
            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
            scheduleTaskExecutor.scheduleAtFixedRate(this::setUpNotificationIcon, 0, 15, TimeUnit.SECONDS);
        }

        return true;
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
                Runnable r = HomeController.getHomeControllerInstance().getNavigationViewOnOptionsItemSelected(this, item.getItemId());
                try {
                    r.run();
                }catch(NullPointerException e){
                    Utilities.stampaToast(FriendsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                }
                return false;
            }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = friendsController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(FriendsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void showFriendsProgressBar(boolean show) {
        runOnUiThread(()-> {
            if (show)
                progressBar.setVisibility(View.VISIBLE);
            else
                progressBar.setVisibility(View.INVISIBLE);
        });
    }

    public void keepSearchViewExpanded() {
        if(searchView != null) {
            runOnUiThread(()-> {
                searchView.clearFocus();
                menu.findItem(R.id.searchItem).expandActionView(); //Verrà collassata in onResume()
            });
        }
    }

    public void setLayoutsForFriends(boolean searchIsExpanded) {
        runOnUiThread(()-> {
            if(searchIsExpanded)
                friendsLinearLayout.setVisibility(View.INVISIBLE);
            else
                friendsLinearLayout.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menu != null && searchView != null) {
            runOnUiThread(()-> {
                menu.findItem(R.id.searchItem).collapseActionView();
                searchView.setIconified(true);
                searchView.clearFocus();
            });

            //La struttura potrebbe essere stata invalidata, per cui si reinizializza il RecyclerView degli amici
            friendsController.initializeActivityFriendsAdapter();
        }

        String profilePicUrl = User.getUserProfilePictureUrl();
        if(profilePicUrl != null)
            refreshProfilePicture(profilePicUrl);
    }

    public void setFriendsRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            friendsRecyclerView.setAdapter(adapter);

            friendsRecyclerView.setItemViewCacheSize(30);

            friendsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            friendsRecyclerView.setFocusable(false);
        });
    }

    public void closeDrawerLayout() {
        runOnUiThread(()-> friendsDrawerLayout.closeDrawer(GravityCompat.START));
    }

    public void openDrawerLayout() {
        runOnUiThread(()-> friendsDrawerLayout.openDrawer(GravityCompat.START));
    }

    public void showEmptyFriendsLayout(boolean show) {
        runOnUiThread(()-> {
            if(show)
                emptyFriendsListTextView.setVisibility(View.VISIBLE);
            else
                emptyFriendsListTextView.setVisibility(View.GONE);
        });
    }
}