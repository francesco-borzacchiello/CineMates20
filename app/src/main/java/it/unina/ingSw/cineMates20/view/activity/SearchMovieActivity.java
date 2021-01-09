package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.SearchMovieController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchMovieActivity extends AppCompatActivity {
    private SearchMovieController searchMovieController;
    private Toolbar toolbar;
    private String searchText;
    private SearchView searchView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                menu.findItem(R.id.searchItem).collapseActionView();
                hideSearchViewFromHome();
                //finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        Bundle srcTxtBundle = getIntent().getExtras();
        if(srcTxtBundle != null) {
            searchText = srcTxtBundle.getString("searchText");
        }
        else { //in caso di loginType null si verrà reindirizzati alla pagina precedente
            finish();
            return;
        }

        searchMovieController = SearchMovieController.getSearchMovieControllerInstance();
        searchMovieController.setSearchMovieActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_search_movies);
        setToolBar();
    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        //Inizializza la searchView con un hint
        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un film");

        //Espandi la searchView ed elimina il focus
        menu.findItem(R.id.searchItem).expandActionView();
        searchView.setQuery(searchText, false);
        searchView.clearFocus();

        //Inizializza listener pressione searchView
        menu.findItem(R.id.searchItem).setOnActionExpandListener(searchMovieController.getSearchViewonActionExpandListener());

        //Inizializza listener query di searchView
        searchView.setOnQueryTextListener(searchMovieController.getSearchViewOnQueryTextListener());

        this.menu = menu;

        return true;
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = searchMovieController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(SearchMovieActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    //Comunica con HomeController e SearchMovieController per poter nascondere la barra di ricerca dalla home
    public void hideSearchViewFromHome() {
        searchMovieController.hideSearchViewFromHome(
                HomeController.getHomeControllerInstance().getHomeActivity());
    }

    public void clearSearchViewFocus() {
        if(searchView != null)
            searchView.clearFocus();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,0);
    }
}