package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.MoviesListController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class MoviesListActivity extends AppCompatActivity {
    private MoviesListController moviesListController;
    private DrawerLayout moviesListDrawerLayout;
    private NavigationView navigationView;
    private ProgressBar progressBar;
    private RecyclerView moviesListRecyclerView;
    private TextView emptyListTextView;
    private Menu menu;
    private Spinner listTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                if(moviesListController.isDeleteEnabled()) {
                    moviesListController.resetAllMoviesCheckBoxes();
                    moviesListController.updateAllMoviesCheckBoxesVisibility();
                } else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        moviesListController = MoviesListController.getMoviesListControllerInstance();
        moviesListController.setMoviesListActivity(this);
        HomeController.getHomeControllerInstance().setMoviesListActivity(this);

        initializeGraphicsComponents();
        configureNavigationDrawer();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_movies_list);
        setToolbar();

        listTypeSpinner = findViewById(R.id.moviesListSpinner);
        listTypeSpinner.setOnItemSelectedListener(moviesListController.getMoviesListActivitySpinnerListener());

        boolean isFavourites = getIntentExtra();
        if(!isFavourites)
            listTypeSpinner.setSelection(1);

        emptyListTextView = findViewById(R.id.emptyMovieListTextView);
        progressBar = findViewById(R.id.progressBarMoviesList);
        moviesListRecyclerView = findViewById(R.id.moviesListRecyclerView);
    }

    private boolean getIntentExtra() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            return bundle.getBoolean("isFavourites");
        }
        else {
            Utilities.stampaToast(this, "Si è verificato un errore.\nRiprova tra qualche minuto.");
            finish();
        }
        return false;
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Le tue liste");
        }
    }

    private void configureNavigationDrawer() {
        moviesListDrawerLayout = findViewById(R.id.moviesListDrawerLayout);
        navigationView = findViewById(R.id.moviesListNavigationView);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);

        runOnUiThread(() -> {
            UserDB user = User.getLoggedUser(this);
            nomeTextView.setText(user.getNome());
            cognomeTextView.setText(user.getCognome());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        menu.findItem(R.id.searchItem).setVisible(false);
        menu.findItem(R.id.notificationItem).setVisible(false);
        menu.findItem(R.id.shareItem).setIcon(R.drawable.ic_delete);
        menu.findItem(R.id.shareItem).setTitle("Elimina");

        menu.findItem(R.id.shareItem).setOnMenuItemClickListener(moviesListController.getOnMenuItemClickListener());

        setNavigationViewActionListener();

        this.menu = menu;

        return true;
    }

    private void setNavigationViewActionListener() {
        //Listener per icone del NavigationView
        navigationView.setNavigationItemSelectedListener(
            item -> {
                Runnable r = HomeController.getHomeControllerInstance().getNavigationViewOnOptionsItemSelected(this, item.getItemId());
                try {
                    r.run();
                }catch(NullPointerException e){
                    Utilities.stampaToast(MoviesListActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                }
                return false;
            }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = moviesListController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(MoviesListActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void setMoviesListRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(() -> {
            moviesListRecyclerView.setAdapter(adapter);

            moviesListRecyclerView.setItemViewCacheSize(30);

            moviesListRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        });
    }

    public String getSelectedSpinnerItem() {
        return listTypeSpinner.getSelectedItem().toString();
    }

    public void hideProgressBar() {
        runOnUiThread(()-> progressBar.setVisibility(View.INVISIBLE));
    }

    public void showProgressBar() {
        runOnUiThread(()-> progressBar.setVisibility(View.VISIBLE));
    }

    public void openDrawerLayout() {
        runOnUiThread(()-> moviesListDrawerLayout.openDrawer(GravityCompat.START));
    }

    public void closeDrawerLayout() {
        runOnUiThread(()-> moviesListDrawerLayout.closeDrawer(GravityCompat.START));
    }

    public boolean areMoviesHidden() {
        return moviesListRecyclerView.getVisibility() == View.INVISIBLE;
    }

    public void setMoviesVisibility(boolean show) {
        runOnUiThread(() -> {
            if (show)
                moviesListRecyclerView.setVisibility(View.VISIBLE);
            else
                moviesListRecyclerView.setVisibility(View.INVISIBLE);
        });
    }

    public void setEmptyMovieListTextViewVisibility(boolean show) {
        runOnUiThread(() -> {
            if(show) {
                emptyListTextView.setVisibility(View.VISIBLE);
                menu.findItem(R.id.shareItem).setVisible(false);
            }
            else {
                emptyListTextView.setVisibility(View.INVISIBLE);
                menu.findItem(R.id.shareItem).setVisible(true);
            }
        });
    }

    public RecyclerView getMoviesRecyclerView() {
        return moviesListRecyclerView;
    }
}