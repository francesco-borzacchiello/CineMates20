package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.SearchMovieActivity;
import it.unina.ingSw.cineMates20.view.adapter.SearchMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchMovieController {
    private static SearchMovieController instance;
    private SearchMovieActivity searchMovieActivity;
    private static TmdbApi tmdbApi;

    private SearchMovieController() {}

    public static SearchMovieController getSearchMovieControllerInstance() {
        if(instance == null)
            instance = new SearchMovieController();
        return instance;
    }

    public void start(Activity parent, String query) {
        Intent intent = new Intent(parent, SearchMovieActivity.class);
        intent.putExtra("searchText", query); //Questa stringa verrà mostrata nella toolbar di SearchMovieActivity
        parent.startActivity(intent);
        parent.overridePendingTransition(0,0);
    }

    public void setSearchMovieActivity(SearchMovieActivity searchMovieActivity) {
        if(searchMovieActivity == null) return;

        this.searchMovieActivity = searchMovieActivity;
        tmdbApi = new TmdbApi(searchMovieActivity.getResources().getString(R.string.themoviedb_api_key));
    }

    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity)) return;

            //TODO: aggiungere la gestione degli altri item del menu, come le notifiche
        };
    }

    public SearchView.OnQueryTextListener getSearchViewOnQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity)) {
                    searchMovieActivity.clearSearchViewFocus();
                    return false;
                }

                searchMovieActivity.showNextSearchFragment(!initializeMovieSearch(query));

                searchMovieActivity.updateSearchQueue(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
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
                searchMovieActivity.finish(); //Termina la corrente sessione di ricerca (@Override finish())
                return false;
            }
        };
    }

    public boolean initializeMovieSearch(String query) {
        searchMovieActivity.clearSearchViewFocus();
        searchMovieActivity.setSearchText(query);

        //tmdbApi = new TmdbApi(searchMovieActivity.getResources().getString(R.string.themoviedb_api_key));
        TmdbSearch search = tmdbApi.getSearch();
        MovieResultsPage movieResults = search.searchMovie(searchMovieActivity.getSearchText(), 0, "it", true, 0);

        initializerAdapterForSearchMovies(movieResults);
        //Se non sono stati trovati risultati, restituisce "false"
        return movieResults.getTotalResults() > 0;
    }

    private void initializerAdapterForSearchMovies(@NotNull MovieResultsPage movieResults) {
        ArrayList<String> titles = new ArrayList<>(),
                          descriptions = new ArrayList<>(),
                          imagesUrl = new ArrayList<>();
        ArrayList<Runnable> threeDotsListeners = new ArrayList<>(),
                            showDetailsMovieListeners = new ArrayList<>();

        for (MovieDb movie : movieResults) {
            threeDotsListeners.add(getThreeDotsListener(movie));
            showDetailsMovieListeners.add(getDetailsMovieListener(movie));

            if(movie.getTitle() != null)
                titles.add(movie.getTitle());
            else
                titles.add(movie.getOriginalTitle());

            descriptions.add(movie.getOverview());
            imagesUrl.add(movie.getPosterPath());
        }

        if (movieResults.getTotalResults() > 0) {
            SearchMovieAdapter adapter = new SearchMovieAdapter(searchMovieActivity, titles,
                    descriptions, imagesUrl, threeDotsListeners, showDetailsMovieListeners);

            adapter.setHasStableIds(true);

            searchMovieActivity.setMoviesRecyclerView(adapter);
        }
        else
            searchMovieActivity.hideSearchMovieProgressBar();
    }

    public void hideSearchMovieProgressBar() {
        searchMovieActivity.hideSearchMovieProgressBar();
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getDetailsMovieListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity)) return;

            searchMovieActivity.showSearchMovieProgressBar();
            ShowDetailsMovieController.getShowDetailsMovieControllerInstance()
                    .start(searchMovieActivity, movie, "SearchMovieActivity");
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getThreeDotsListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity)) return;
            searchMovieActivity.createAndShowBottomMenuFragment(movie.getPosterPath(), movie.getOriginalTitle());
        };
    }

    public void resetHomeRecyclerViewPosition() {
        HomeController.getHomeControllerInstance().resetHomeRecyclerViewPosition();
    }
}
