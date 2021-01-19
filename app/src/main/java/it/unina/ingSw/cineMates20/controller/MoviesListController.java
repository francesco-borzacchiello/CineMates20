package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.MoviesListActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeStyleMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class MoviesListController {
    //region Attributi
    private static MoviesListController instance;
    private MoviesListActivity moviesListActivity;
    private HomeStyleMovieAdapter actualAdapter;
    //endregion

    //region Costruttore
    private MoviesListController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity, boolean isFavourites) {
        Intent intent = new Intent(activity, MoviesListActivity.class);
        intent.putExtra("isFavourites", isFavourites);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //endregion

    //region getInstance() per il pattern singleton
    public static MoviesListController getMoviesListControllerInstance() {
        if(instance == null)
            instance = new MoviesListController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setMoviesListActivity(@NonNull MoviesListActivity moviesListActivity) {
        this.moviesListActivity = moviesListActivity;
    }
    //endregion


    public AdapterView.OnItemSelectedListener getSpinnerOnItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initializeActivityMovies(parent.getItemAtPosition(position).toString().equals("Preferiti"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    //TODO: da modificare con film reali della lista dell'utente dopo aver completato applicativo server
    public void initializeActivityMovies(boolean isFavourites) {
        //if(!listaSelezionata(isFavourites).isEmpty())
             /*if(moviesListActivity.areMoviesHidden()) {
                  moviesListActivity.setMoviesVisibility(true);
                  moviesListActivity.setEmptyMovieListTextViewVisibility(false);
               }
              */
              //...recupero film dal nostro DB, poi da TMDB, e infine creazione e set adapter per il RecyclerView
        /*else {
              moviesListActivity.setMoviesVisibility(false);
              moviesListActivity.setEmptyMovieListTextViewVisibility(true);
         */

        //TODO: rimuovere questo codice temporaneo successivamente e sostituirlo con quello sopra
        Random rd = new Random();
        boolean tmp = rd.nextBoolean();

        if(!tmp) { //if(!listaVuota)
            if(moviesListActivity.areMoviesHidden()) {
                moviesListActivity.setMoviesVisibility(true);
                moviesListActivity.setEmptyMovieListTextViewVisibility(false);
            }

            TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(moviesListActivity.getResources().getString(R.string.themoviedb_api_key)));
            MovieResultsPage upcomingUsa = tmdbMovies.getUpcoming("it", 1, "US");
            ArrayList<String> upcomingTitles = new ArrayList<>(),
                              upcomingImagesUrl = new ArrayList<>();
            ArrayList<Runnable> upcomingMoviesCardViewListeners = new ArrayList<>();
            ArrayList<Integer> moviesIds = new ArrayList<>();

            initializeListsForMoviesListAdapter(upcomingUsa, upcomingTitles,
                            upcomingImagesUrl, upcomingMoviesCardViewListeners, moviesIds);

            actualAdapter = getMoviesListRecyclerViewAdapter
                    (upcomingUsa, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners, moviesIds);

            if(actualAdapter != null)
                moviesListActivity.setMoviesListRecyclerView(actualAdapter);
        }
        else {
            moviesListActivity.setMoviesVisibility(false);
            moviesListActivity.setEmptyMovieListTextViewVisibility(true);
        }

    }

    //Non occorre eliminare questo metodo
    public void showEmptyMovieList() {
        moviesListActivity.setMoviesVisibility(false);
        moviesListActivity.setEmptyMovieListTextViewVisibility(true);
    }

    public void initializeListsForMoviesListAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                   @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                   @NotNull ArrayList<Runnable> movieCardViewListeners,
                                                   @NotNull ArrayList<Integer> moviesIds) {
        for (MovieDb movie : movieResultsPage) {
            moviesIds.add(movie.getId());
            movieCardViewListeners.add(getMovieCardViewListener(movie));

            if(movie.getTitle() != null)
                titles.add(movie.getTitle());
            else
                titles.add(movie.getOriginalTitle());

            moviesImagesUrl.add(movie.getPosterPath());

            if(titles.size() > 19)
                break;
        }
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getMovieCardViewListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(moviesListActivity)) return;

            moviesListActivity.showMoviesListProgressBar();
            ShowDetailsMovieController.getShowDetailsMovieControllerInstance()
                    .start(moviesListActivity, movie, "MoviesListActivity");
        };
    }

    @Nullable
    public HomeStyleMovieAdapter getMoviesListRecyclerViewAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                                  @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                                  @NotNull ArrayList<Runnable> movieCardViewListeners,
                                                                  @NotNull ArrayList<Integer> moviesIds) {
        if (movieResultsPage.getTotalResults() > 0) {
            HomeStyleMovieAdapter homeStyleMovieAdapter = new HomeStyleMovieAdapter(moviesListActivity, titles,
                    moviesImagesUrl, movieCardViewListeners, moviesIds);

            homeStyleMovieAdapter.setHasStableIds(true);

            return homeStyleMovieAdapter;
        }
        else
            return null;
    }

    //Restituisce un listener le icone della toolbar in MoviesListActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(moviesListActivity)) return;

            if(itemId == android.R.id.home)
                moviesListActivity.openDrawerLayout();
        };
    }

    public void hideMoviesListProgressBar() {
        if(moviesListActivity != null)
            moviesListActivity.hideMoviesListProgressBar();
    }

    public RecyclerView getMoviesRecyclerView() {
        return moviesListActivity.getMoviesRecyclerView();
    }

    public void updateAllMoviesCheckBoxesVisibility() {
        actualAdapter.updateVisibility();
        actualAdapter.notifyDataSetChanged();
    }

    public boolean isDeleteEnabled() {
        return actualAdapter.isDeleteEnabled();
    }

    public MenuItem.OnMenuItemClickListener getOnMenuItemClickListener() {
        return item -> {
            if(Utilities.checkNullActivityOrNoConnection(moviesListActivity)) return true;

            if(actualAdapter.isDeleteEnabled()) {
                actualAdapter.deleteSelectedItem();

                moviesListActivity.getMoviesRecyclerView().getRecycledViewPool().clear();
            }
            updateAllMoviesCheckBoxesVisibility();
            return true;
        };
    }

    public void resetAllMoviesCheckBoxes() {
        actualAdapter.resetAllMoviesCheckBoxes();
    }
}
