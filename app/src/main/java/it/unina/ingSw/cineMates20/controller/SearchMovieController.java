package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.ListaFilmDB;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.activity.SearchMovieActivity;
import it.unina.ingSw.cineMates20.view.adapter.SearchMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SearchMovieController {
    private static SearchMovieController instance;
    private MoviesListController movieListController;
    private SearchMovieActivity searchMovieActivity;
    private static TmdbApi tmdbApi;

    private SearchMovieController() {
        movieListController = MoviesListController.getMoviesListControllerInstance();
    }

    public static SearchMovieController getSearchMovieControllerInstance() {
        if(instance == null)
            instance = new SearchMovieController();
        return instance;
    }

    public void start(Activity parent, String query) {
        Thread t = new Thread(()->
                tmdbApi = new TmdbApi(parent.getResources().getString(R.string.themoviedb_api_key)));
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        Intent intent = new Intent(parent, SearchMovieActivity.class);
        intent.putExtra("searchText", query); //Questa stringa verrà mostrata nella toolbar di SearchMovieActivity
        parent.startActivity(intent);
        parent.overridePendingTransition(0,0);
    }

    public void setSearchMovieActivity(SearchMovieActivity searchMovieActivity) {
        this.searchMovieActivity = searchMovieActivity;
    }

    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity)) return;

            if(itemId == R.id.notificationItem)
                NotificationsController.getNotificationControllerInstance().start(searchMovieActivity);
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

        Integer[] movieResultsCount = new Integer[1];
        movieResultsCount[0] = 0;

        Thread t = new Thread(()-> {
            if(tmdbApi == null)
                tmdbApi = new TmdbApi(searchMovieActivity.getResources().getString(R.string.themoviedb_api_key));

            TmdbSearch search = tmdbApi.getSearch();
            MovieResultsPage movieResults = search.searchMovie(searchMovieActivity.getSearchText(), 0, "it", true, 0);
            movieResultsCount[0] = movieResults.getTotalResults();

            initializeAdapterForMovieSearch(movieResults);
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        //Se non sono stati trovati risultati, restituisce "false"
        return movieResultsCount[0] > 0;
    }

    private void initializeAdapterForMovieSearch(@NotNull MovieResultsPage movieResults) {
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
                    .start(searchMovieActivity, movie);
        };
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getThreeDotsListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(searchMovieActivity)) return;
            searchMovieActivity.createAndShowBottomMenuFragment(movie);
        };
    }

    public void resetHomeRecyclerViewPosition() {
        HomeController.getHomeControllerInstance().resetHomeRecyclerViewPosition();
    }

    public View.OnClickListener getReportOnClickListener(MovieDb movie) {
        return (view) -> {
            searchMovieActivity.closeBottomMenu();
            ReportController.getReportControllerInstance().startMovieReport(searchMovieActivity, movie);
        };
    }

    public boolean isSelectedMovieAlreadyInList(MovieDb movie, boolean isFavouritesList) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        String email = User.getUserInstance(searchMovieActivity).getLoggedUser().getEmail();
        boolean[] contains = new boolean[1];

        Thread t = new Thread(()-> {
            String url;
            if(isFavouritesList)
               url = searchMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/getPreferitiByPossessore/{FK_Possessore}";
            else
               url = searchMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/getDaVedereByPossessore/{FK_Possessore}";

            ListaFilmDB listaFilm = restTemplate.getForObject(url, ListaFilmDB.class, email);

            url = searchMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/containsFilm/{id}/{FK_Film}";

            contains[0] = restTemplate.getForObject(url, boolean.class, listaFilm.getId(), movie.getId());
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return contains[0];
    }

    public void closeBottonMenu() {
        searchMovieActivity.closeBottomMenu();
    }

    public View.OnClickListener getAggiungiPreferitiOnClickListener(MovieDb movie) {
        return v -> {
            movieListController.aggiungiFilmAPreferiti(movie, searchMovieActivity);
            closeBottonMenu();
        };
    }

    public View.OnClickListener getRimuoviPreferitiOnClickListener(MovieDb movie) {
        return v -> {
            movieListController.rimuoviFilmDaPreferiti(movie, searchMovieActivity);
            closeBottonMenu();
        };
    }

    public View.OnClickListener getAggiungiDaVedereOnClickListener(MovieDb movie) {
        return v -> {
             movieListController.aggiungiFilmInDaVedere(movie, searchMovieActivity);
            closeBottonMenu();
        };
    }

    public View.OnClickListener getRimuoviDaVedereOnClickListener(MovieDb movie) {
        return v -> {
            movieListController.rimuoviFilmDaVedere(movie, searchMovieActivity);
            closeBottonMenu();
        };
    }
}