package it.unina.ingSw.cineMates20.controller;

import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import org.jetbrains.annotations.NotNull;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.activity.SearchMovieActivity;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchMovieController {
    private static SearchMovieController instance;
    private SearchMovieActivity searchMovieActivity;
    private static TmdbMovies tmdbMovies;

    private SearchMovieController() {}

    public static SearchMovieController getSearchMovieControllerInstance() {
        if(instance == null)
            initializeNewInstance();
        return instance;
    }

    private static void initializeNewInstance() {
        instance = new SearchMovieController();
        tmdbMovies = new TmdbApi("b3a2d6a56e6622052bbf6936ff2ace73").getMovies();  //new TmdbApi(getResources().getString(R.string.themoviedb_api_key).getMovies());
        //Esempio d'uso: testato
        //MovieDb movie = tmdbMovies.getMovie(5353, "it");
        //Log.i("TESTTHEMOVIEDB", movie.getOriginalTitle());
    }

    public void setSearchMovieActivity(SearchMovieActivity searchMovieActivity) {
        this.searchMovieActivity = searchMovieActivity;
    }

    public void hideSearchViewFromHome(@NotNull HomeActivity homeActivity) {
        homeActivity.hideSearchView();
    }

    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity))
                searchMovieActivity.runOnUiThread(() -> Utilities.stampaToast(searchMovieActivity, "Si è verificato un errore.\nVerifica la tua connessione."));

            if(itemId == android.R.id.home)
                handleOnBackPressed();
            //else if(itemId == ...)

            //TODO: aggiungere la gestione degli altri item del menu, come la search (invio richiesta a themoviedb)...
        };
    }

    private void handleOnBackPressed() {
        searchMovieActivity.hideSearchViewFromHome();
        searchMovieActivity.finish();
    }

    public SearchView.OnQueryTextListener getSearchViewOnQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMovieActivity.clearSearchViewFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        };
    }

    public MenuItem.OnActionExpandListener getSearchViewonActionExpandListener() {
        return new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchMovieActivity.hideSearchViewFromHome();
                searchMovieActivity.finish();
                return false;
            }
        };
    }
}
