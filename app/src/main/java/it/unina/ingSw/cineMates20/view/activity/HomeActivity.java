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
import androidx.recyclerview.widget.LinearLayoutManager;
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
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.SettingsController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class HomeActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout homeDrawerLayout;
    private HomeController homeController;
    private SearchView searchView;
    private Menu menu;
    private ProgressBar progressBar;
    private TextView nowPlayingLabelHomeMoviesTextView,
                     mostPopularMoviesTextView,
                     upcomingMoviesTextView,
                     topRatedMoviesTextView;

    private RecyclerView nowPlayingHomeMoviesRecyclerView,
                         mostPopularHomeMoviesRecyclerView,
                         upcomingHomeMoviesRecyclerView,
                         topRatedHomeMoviesRecyclerView;
    private LinearLayout homeLinearLayout;
    private ImageView profilePicture;
    private MenuItem notificationItem;
    private ScheduledExecutorService scheduleTaskExecutor;
    private boolean notificationTaskIsAlive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeController = HomeController.getHomeControllerInstance();
        homeController.setHomeActivity(this);

        initializeGraphicsComponents();
        configureNavigationDrawer();

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                if(homeDrawerLayout.isOpen())
                    closeDrawerLayout();
                else
                    finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_home);
        setHomeToolbar();

        nowPlayingHomeMoviesRecyclerView = findViewById(R.id.nowShowingHomeMoviesRecyclerView);
        mostPopularHomeMoviesRecyclerView = findViewById(R.id.mostPopularHomeMoviesRecyclerView);
        upcomingHomeMoviesRecyclerView = findViewById(R.id.upcomingHomeMoviesRecyclerView);
        topRatedHomeMoviesRecyclerView = findViewById(R.id.topRatedHomeMoviesRecyclerView);

        //Le seguenti TextView saranno rese visibili non appena la Home sarà pronta
        nowPlayingLabelHomeMoviesTextView = findViewById(R.id.nowShowingLabelHomeMovies);
        nowPlayingLabelHomeMoviesTextView.setVisibility(View.INVISIBLE);

        mostPopularMoviesTextView = findViewById(R.id.mostPopularMovies);
        mostPopularMoviesTextView.setVisibility(View.INVISIBLE);

        upcomingMoviesTextView = findViewById(R.id.upcomingMovies);
        upcomingMoviesTextView.setVisibility(View.INVISIBLE);

        topRatedMoviesTextView = findViewById(R.id.topRatedMovies);
        topRatedMoviesTextView.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progressBarHomeMovies);
        progressBar.setVisibility(View.INVISIBLE);

        homeController.setHomeActivityMovies();
        homeLinearLayout = findViewById(R.id.linearLayoutHome);
    }

    private void setHomeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Home");
        }
    }

    private void configureNavigationDrawer() {
        homeDrawerLayout = findViewById(R.id.homeDrawerLayout);
        navigationView = findViewById(R.id.homeNavigationView);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);
        profilePicture = navigationView.getHeaderView(0).findViewById(R.id.imageProfile);

        runOnUiThread(() -> {
            nomeTextView.setText(User.getLoggedUser(this).getNome());
            cognomeTextView.setText(User.getLoggedUser(this).getCognome());

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
        searchView.setQueryHint("Cerca un film");

        searchView.setOnQueryTextListener(homeController.getSearchViewOnQueryTextListener());
        searchView.setOnQueryTextFocusChangeListener(homeController.getSearchViewOnQueryTextFocusChangeListener());
        searchView.setOnSearchClickListener(homeController.getOnSearchClickListener());
        notificationItem = menu.findItem(R.id.notificationItem);

        setNavigationViewActionListener();
        this.menu = menu;

        if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled()) {
            notificationTaskIsAlive = true;
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
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
                Runnable r = homeController.getNavigationViewOnOptionsItemSelected(this, item.getItemId());
                try {
                    r.run();
                }catch(NullPointerException e){
                    Utilities.stampaToast(HomeActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                }
                return false;
            }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = homeController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(HomeActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void closeDrawerLayout() {
        runOnUiThread(()-> homeDrawerLayout.closeDrawer(GravityCompat.START));
    }

    public void openDrawerLayout() {
        runOnUiThread(()-> homeDrawerLayout.openDrawer(GravityCompat.START));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menu != null && searchView != null) {
            menu.findItem(R.id.searchItem).collapseActionView();
            searchView.setIconified(true);
            searchView.clearFocus();
        }

        String profilePicUrl = User.getUserProfilePictureUrl();
        if(profilePicUrl != null)
            refreshProfilePicture(profilePicUrl);

        if(notificationItem == null) return;

        if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled() && !notificationTaskIsAlive) {
            notificationTaskIsAlive = true;
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
            scheduleTaskExecutor.scheduleAtFixedRate(this::setUpNotificationIcon, 0, 15, TimeUnit.SECONDS);
        }
        else if(!SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled() && notificationTaskIsAlive) {
            notificationTaskIsAlive = false;
            scheduleTaskExecutor.shutdownNow();
        }
    }

    public void setNowPlayingHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            nowPlayingHomeMoviesRecyclerView.setAdapter(adapter);

            nowPlayingHomeMoviesRecyclerView.setHasFixedSize(true);
            nowPlayingHomeMoviesRecyclerView.setItemViewCacheSize(20);

            nowPlayingHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                    (this, LinearLayoutManager.HORIZONTAL, false));
        });
    }

    public void setMostPopularHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            mostPopularHomeMoviesRecyclerView.setAdapter(adapter);

            mostPopularHomeMoviesRecyclerView.setHasFixedSize(true);
            mostPopularHomeMoviesRecyclerView.setItemViewCacheSize(20);

            mostPopularHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                    (this, LinearLayoutManager.HORIZONTAL, false));
        });
    }

    public void setUpcomingHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            upcomingHomeMoviesRecyclerView.setAdapter(adapter);

            upcomingHomeMoviesRecyclerView.setHasFixedSize(true);
            upcomingHomeMoviesRecyclerView.setItemViewCacheSize(20);

            upcomingHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                    (this, LinearLayoutManager.HORIZONTAL, false));
        });
    }

    public void setTopRatedHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            topRatedHomeMoviesRecyclerView.setAdapter(adapter);

            topRatedHomeMoviesRecyclerView.setHasFixedSize(true);
            topRatedHomeMoviesRecyclerView.setItemViewCacheSize(20);

            topRatedHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                    (this, LinearLayoutManager.HORIZONTAL, false));
        });
    }

    public void showHomeTextViews() {
        runOnUiThread(()-> {
            if(nowPlayingHomeMoviesRecyclerView.getAdapter() != null &&
                    nowPlayingHomeMoviesRecyclerView.getAdapter().getItemCount() > 0) {

                nowPlayingLabelHomeMoviesTextView.setVisibility(View.VISIBLE);

                nowPlayingLabelHomeMoviesTextView.setAlpha(0.0f);
                nowPlayingLabelHomeMoviesTextView.animate().alpha(1.0f);
            }

            if(mostPopularHomeMoviesRecyclerView.getAdapter() != null &&
                    mostPopularHomeMoviesRecyclerView.getAdapter().getItemCount() > 0) {

                mostPopularMoviesTextView.setVisibility(View.VISIBLE);

                mostPopularMoviesTextView.setAlpha(0.0f);
                mostPopularMoviesTextView.animate().alpha(1.0f);
            }

            if(upcomingHomeMoviesRecyclerView.getAdapter() != null &&
                    upcomingHomeMoviesRecyclerView.getAdapter().getItemCount() > 0) {

                upcomingMoviesTextView.setVisibility(View.VISIBLE);

                upcomingMoviesTextView.setAlpha(0.0f);
                upcomingMoviesTextView.animate().alpha(1.0f);
            }

            if(topRatedHomeMoviesRecyclerView.getAdapter() != null &&
                    topRatedHomeMoviesRecyclerView.getAdapter().getItemCount() > 0) {

                topRatedMoviesTextView.setVisibility(View.VISIBLE);

                topRatedMoviesTextView.setAlpha(0.0f);
                topRatedMoviesTextView.animate().alpha(1.0f);
            }
        });
    }

    public void resetRecyclersViewPosition() {
        runOnUiThread(()-> {
            nowPlayingHomeMoviesRecyclerView.smoothScrollToPosition(0);
            mostPopularHomeMoviesRecyclerView.smoothScrollToPosition(0);
            upcomingHomeMoviesRecyclerView.smoothScrollToPosition(0);
            topRatedHomeMoviesRecyclerView.smoothScrollToPosition(0);
        });
    }

    public void hideMovieProgressBar() {
        runOnUiThread(()-> progressBar.setVisibility(View.INVISIBLE));
    }

    public void showMovieProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void keepSearchViewExpanded() {
        if(searchView != null)
            runOnUiThread(()-> {
                searchView.clearFocus();
                menu.findItem(R.id.searchItem).expandActionView(); //Verrà collassata in onResume()
            });
    }

    public void setLayoutsForHome(boolean searchIsExpanded) {
        runOnUiThread(()-> {
            if(searchIsExpanded)
                homeLinearLayout.setVisibility(View.INVISIBLE);
            else
                homeLinearLayout.setVisibility(View.VISIBLE);
        });
    }
}