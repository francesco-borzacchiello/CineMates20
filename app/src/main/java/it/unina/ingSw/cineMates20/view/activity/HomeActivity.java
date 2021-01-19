package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class HomeActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout homeDrawerLayout;
    private HomeController homeController;
    private Toolbar toolbar;
    private SearchView searchView;
    private Menu menu;
    private ProgressBar progressBar;
    private TextView nowShowingLabelHomeMoviesTextView,
                     mostPopularMoviesTextView,
                     upcomingMoviesTextView,
                     topRatedMoviesTextView;

    private RecyclerView nowPlayingHomeMoviesRecyclerView,
                         mostPopularHomeMoviesRecyclerView,
                         upcomingHomeMoviesRecyclerView,
                         topRatedHomeMoviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeController = HomeController.getHomeControllerInstance();
        homeController.setHomeActivity(this);

        initializeGraphicsComponents();
        configureNavigationDrawer();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_home);
        setHomeToolbar();

        nowPlayingHomeMoviesRecyclerView = findViewById(R.id.nowShowingHomeMoviesRecyclerView);
        mostPopularHomeMoviesRecyclerView = findViewById(R.id.mostPopularHomeMoviesRecyclerView);
        upcomingHomeMoviesRecyclerView = findViewById(R.id.upcomingHomeMoviesRecyclerView);
        topRatedHomeMoviesRecyclerView = findViewById(R.id.topRatedHomeMoviesRecyclerView);

        //Le seguenti TextView saranno rese visibili non appena la Home sarà pronta
        nowShowingLabelHomeMoviesTextView = findViewById(R.id.nowShowingLabelHomeMovies);
        nowShowingLabelHomeMoviesTextView.setVisibility(View.INVISIBLE);

        mostPopularMoviesTextView = findViewById(R.id.mostPopularMovies);
        mostPopularMoviesTextView.setVisibility(View.INVISIBLE);

        upcomingMoviesTextView = findViewById(R.id.upcomingMovies);
        upcomingMoviesTextView.setVisibility(View.INVISIBLE);

        topRatedMoviesTextView = findViewById(R.id.topRatedMovies);
        topRatedMoviesTextView.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progressBarHomeMovies);
        progressBar.setVisibility(View.INVISIBLE);

        homeController.setHomeActivityMovies();
    }

    private void setHomeToolbar() {
        toolbar = findViewById(R.id.toolbarHeader);
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
        homeController.setCurrentUserInformations();
    }

    public void setCurrentUserDrawerInformations(String nome, String cognome) {
        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);

        runOnUiThread(() -> {
            nomeTextView.setText(nome);
            cognomeTextView.setText(cognome);
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

        setNavigationViewActionListener();
        this.menu = menu;

        return true;
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
        homeDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void openDrawerLayout() {
        homeDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menu != null && searchView != null) {
            menu.findItem(R.id.searchItem).collapseActionView();
            searchView.setIconified(true);
            searchView.clearFocus();
        }
    }

    public void setNowPlayingHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        nowPlayingHomeMoviesRecyclerView.setAdapter(adapter);

        nowPlayingHomeMoviesRecyclerView.setHasFixedSize(true);
        nowPlayingHomeMoviesRecyclerView.setItemViewCacheSize(20);

        nowPlayingHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setMostPopularHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        mostPopularHomeMoviesRecyclerView.setAdapter(adapter);

        mostPopularHomeMoviesRecyclerView.setHasFixedSize(true);
        mostPopularHomeMoviesRecyclerView.setItemViewCacheSize(20);

        mostPopularHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setUpcomingHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        upcomingHomeMoviesRecyclerView.setAdapter(adapter);

        upcomingHomeMoviesRecyclerView.setHasFixedSize(true);
        upcomingHomeMoviesRecyclerView.setItemViewCacheSize(20);

        upcomingHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void setTopRatedHomeMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        topRatedHomeMoviesRecyclerView.setAdapter(adapter);

        topRatedHomeMoviesRecyclerView.setHasFixedSize(true);
        topRatedHomeMoviesRecyclerView.setItemViewCacheSize(20);

        topRatedHomeMoviesRecyclerView.setLayoutManager(new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void showHomeTextViews() {
        nowShowingLabelHomeMoviesTextView.setVisibility(View.VISIBLE);
        nowShowingLabelHomeMoviesTextView.setAlpha(0.0f);
        nowShowingLabelHomeMoviesTextView.animate().alpha(1.0f);

        mostPopularMoviesTextView.setVisibility(View.VISIBLE);
        mostPopularMoviesTextView.setAlpha(0.0f);
        mostPopularMoviesTextView.animate().alpha(1.0f);

        upcomingMoviesTextView.setVisibility(View.VISIBLE);
        upcomingMoviesTextView.setAlpha(0.0f);
        upcomingMoviesTextView.animate().alpha(1.0f);

        topRatedMoviesTextView.setVisibility(View.VISIBLE);
        topRatedMoviesTextView.setAlpha(0.0f);
        topRatedMoviesTextView.animate().alpha(1.0f);
    }

    public void resetRecyclersViewPosition() {
        nowPlayingHomeMoviesRecyclerView.smoothScrollToPosition(0);
        mostPopularHomeMoviesRecyclerView.smoothScrollToPosition(0);
        upcomingHomeMoviesRecyclerView.smoothScrollToPosition(0);
        topRatedHomeMoviesRecyclerView.smoothScrollToPosition(0);
    }

    public void hideMovieProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void showMovieProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void keepSearchViewExpanded() {
        if(searchView != null) {
            searchView.clearFocus();
            menu.findItem(R.id.searchItem).expandActionView(); //Verrà collassata in onResume()
        }
    }

    public void setLayoutsForHome(boolean searchIsExpanded) {
        LinearLayout ll = findViewById(R.id.linearLayoutHome);
        ConstraintLayout cl = findViewById(R.id.constraintLayoutHome);

        if(searchIsExpanded) {
            ll.setVisibility(View.INVISIBLE);
            cl.setBackgroundColor(getResources().getColor(R.color.lightGray));
        }
        else {
            ll.setVisibility(View.VISIBLE);
            cl.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }
}