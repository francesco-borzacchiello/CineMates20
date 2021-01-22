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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.JoinedMoviesController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class JoinedMoviesActivity extends AppCompatActivity {
    private JoinedMoviesController joinedMoviesController;
    private TextView emptyListTextView;
    private ProgressBar progressBar;
    private RecyclerView moviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        joinedMoviesController = JoinedMoviesController.getJoinedMoviesControllerInstance();
        joinedMoviesController.setJoinedMoviesActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_movies_list);
        setToolbar();

        Spinner listType = findViewById(R.id.moviesListSpinner);
        listType.setOnItemSelectedListener(joinedMoviesController.getJoinedMoviesActivitySpinnerListener());

        emptyListTextView = findViewById(R.id.emptyMovieListTextView);
        progressBar = findViewById(R.id.progressBarMoviesList);
        progressBar.setVisibility(View.VISIBLE);
        moviesRecyclerView = findViewById(R.id.moviesListRecyclerView);

        DrawerLayout drawerLayout = findViewById(R.id.moviesListDrawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setTitle("Film in comune");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        menu.findItem(R.id.searchItem).setVisible(false);
        menu.findItem(R.id.notificationItem).setVisible(false);
        menu.findItem(R.id.shareItem).setVisible(false);

        return true;
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = joinedMoviesController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(JoinedMoviesActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void setJoinedMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            moviesRecyclerView.setAdapter(adapter);
            moviesRecyclerView.setItemViewCacheSize(30);
            moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        });
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public boolean areMoviesHidden() {
        return moviesRecyclerView.getVisibility() == View.INVISIBLE;
    }

    public void setMoviesVisibility(boolean show) {
        if(show)
            moviesRecyclerView.setVisibility(View.VISIBLE);
        else
            moviesRecyclerView.setVisibility(View.INVISIBLE);
    }

    public void setEmptyMovieListTextViewVisibility(boolean show) {
        if(show)
            emptyListTextView.setVisibility(View.VISIBLE);
        else
            emptyListTextView.setVisibility(View.INVISIBLE);
    }
}
