package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import info.movito.themoviedbapi.model.MovieDb;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.SearchMovieController;
import it.unina.ingSw.cineMates20.view.fragment.FragmentSearchEmpty;
import it.unina.ingSw.cineMates20.view.fragment.FragmentSearchNotEmpty;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchMovieActivity extends AppCompatActivity {
    private SearchMovieController searchMovieController;
    private NavigationView navigationView;
    private BottomSheetDialog bottomMenuDialog;
    private DrawerLayout drawerLayout;
    private FragmentSearchNotEmpty fragmentSearchNotEmpty;
    private FragmentSearchEmpty fragmentSearchEmpty;
    private FragmentManager manager;
    private String searchText;
    private SearchView searchView;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> currentRecyclerViewAdapter;
    private LinkedList<String> searchHistory;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movies);

        initializeGraphicsComponents();

        Bundle srcTxtBundle = getIntent().getExtras();
        if(srcTxtBundle != null) {
            searchText = srcTxtBundle.getString("searchText");
            searchHistory = new LinkedList<>();
            searchHistory.offer(searchText);
        }
        else {
            finish();
            return;
        }

        searchMovieController = SearchMovieController.getSearchMovieControllerInstance();
        searchMovieController.setSearchMovieActivity(this);
        HomeController.getHomeControllerInstance().setSearchMovieActivity(this);

        //Inizializzo l'unica istanza di FragmentSearchEmpty necessaria
        fragmentSearchEmpty = new FragmentSearchEmpty();

        configureNavigationDrawer();
    }

    private void configureNavigationDrawer() {
        navigationView = findViewById(R.id.navigationViewSearchMovies);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);
        List<String> info = Utilities.getCurrentUserInformations(this);

        if(info.size() > 0)
            runOnUiThread(() -> {
                nomeTextView.setText(info.get(0));
                cognomeTextView.setText(info.get(1));
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        //Inizializza la searchView con un hint
        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un film");

        //La ricerca ha prodotto risultati
        if(searchMovieController.initializeMovieSearch(searchText)) {
            fragmentSearchNotEmpty = new FragmentSearchNotEmpty(currentRecyclerViewAdapter);
            createFirstFragment(fragmentSearchNotEmpty);
        }
        else {
            createFirstFragment(fragmentSearchEmpty);
        }

        //Espandi la searchView ed elimina il focus
        menu.findItem(R.id.searchItem).expandActionView();
        searchView.setQuery(searchText, false);
        searchView.clearFocus();

        //Inizializza listener searchView
        menu.findItem(R.id.searchItem).setOnActionExpandListener(searchMovieController.getSearchViewOnActionExpandListener());

        //Inizializza listener query di searchView
        searchView.setOnQueryTextListener(searchMovieController.getSearchViewOnQueryTextListener());

        setNavigationViewActionListener();

        return true;
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
                        Utilities.stampaToast(SearchMovieActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                    }
                    return false;
                }
        );
    }

    private void initializeGraphicsComponents() {
        drawerLayout = findViewById(R.id.searchMovieNavMenuDrawerLayout);
        progressBar = findViewById(R.id.progressBarSearchMovies);
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setTitle("");
        }
    }

    //Inizializza per la prima volta il fragment dell'activity
    private void createFirstFragment(Fragment fragment) {
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.showMoviesFrameLayout, fragment);
        transaction.commit();
    }

    //Crea una nuova istanza del fragment FragmentBottomMenu e la mostra
    public void createAndShowBottomMenuFragment(MovieDb movie) {
        bottomMenuDialog = new BottomSheetDialog(
                this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_bottom_menu, findViewById(R.id.bottomMenuContainer));

        bottomMenuDialog.setContentView(bottomSheetView);

        LinearLayout preferitiLinearLayout = bottomMenuDialog.findViewById(R.id.layoutFilmPreferiti);
        LinearLayout daVedereLinearLayout = bottomMenuDialog.findViewById(R.id.layoutFilmDaVedere);
        LinearLayout segnalaFilmLayout = bottomMenuDialog.findViewById(R.id.layoutSegnalazioneFilm);

        if(preferitiLinearLayout == null || daVedereLinearLayout == null || segnalaFilmLayout == null) return;
        segnalaFilmLayout.setOnClickListener(searchMovieController.getReportOnClickListener(movie));

        TextView titleTextView = bottomMenuDialog.findViewById(R.id.titoloBottomMenu);
        if(titleTextView != null && movie.getTitle() != null)
            titleTextView.setText(movie.getTitle());
        else if(titleTextView != null)
            titleTextView.setText(movie.getOriginalTitle());

        //TODO: Set listener sui tasti aggiungi/rimuovi da lista "da vedere" attraverso controller lista film

        TextView addToFavouritesTextView = bottomMenuDialog.findViewById(R.id.addToFavouritesTextView);
        TextView addToWatchTextView = bottomMenuDialog.findViewById(R.id.addToWatchTextView);

        if(addToFavouritesTextView == null || addToWatchTextView == null) return;

        if(movie.getPosterPath() != null) {
            ImageView coverImageView = bottomMenuDialog.findViewById(R.id.copertinaBottomMenu);
            Picasso.get().load(getResources().getString(R.string.first_path_poster_image) + movie.getPosterPath()).
                    resize(270, 360)
                    .noFade().into(coverImageView);
        }

        bottomMenuDialog.show();

        if(searchMovieController.isSelectedMovieAlreadyInList(movie, true)) {
            addToFavouritesTextView.setText(getResources().getString(R.string.removeFromFavourites));
            preferitiLinearLayout.setOnClickListener(searchMovieController.getRimuoviPreferitiOnClickListner(movie));
        } else
            preferitiLinearLayout.setOnClickListener(searchMovieController.getAggiungiPreferitiOnClickListner(movie));


        if(searchMovieController.isSelectedMovieAlreadyInList(movie, false)) {
            addToWatchTextView.setText(getResources().getString(R.string.removeFromToWatch));
            daVedereLinearLayout.setOnClickListener(searchMovieController.getRimuoviDaVedereOnClickListner(movie));
        } else
            daVedereLinearLayout.setOnClickListener(searchMovieController.getAggiungiDaVedereOnClickListner(movie));
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

            fragmentSearchNotEmpty = new FragmentSearchNotEmpty(currentRecyclerViewAdapter);

            transaction.replace(R.id.showMoviesFrameLayout, fragmentSearchNotEmpty);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else if(!fragmentSearchEmpty.isVisible()) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.showMoviesFrameLayout, fragmentSearchEmpty);
            transaction.addToBackStack(null);
            transaction.commit();
        }
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

    public void clearSearchViewFocus() {
        searchView.setFocusable(false);
        searchView.setIconified(false);
        searchView.clearFocus();
    }

    @Override
    public void finish() {
        if (manager.getBackStackEntryCount() == 0) {
            super.finish();
            searchMovieController.resetHomeRecyclerViewPosition();
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

    public String getSearchText(){
        return searchText;
    }

    public void setSearchText(String query) {
        if(query != null && !query.equals(""))
            searchText = query;
    }

    public void setMoviesRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        currentRecyclerViewAdapter = adapter;
    }

    public void updateSearchQueue(String query) {
        searchHistory.offer(query);
    }

    public void hideSearchMovieProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void showSearchMovieProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void closeDrawerLayout() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void closeBottomMenu() {
        bottomMenuDialog.dismiss();
    }
}