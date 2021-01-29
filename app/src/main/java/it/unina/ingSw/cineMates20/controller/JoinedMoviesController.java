package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

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
import it.unina.ingSw.cineMates20.view.activity.JoinedMoviesActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeStyleMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class JoinedMoviesController {
    //region Attributi
    private static JoinedMoviesController instance;
    private JoinedMoviesActivity joinedMoviesActivity;
    private HomeStyleMovieAdapter actualAdapter;
    //endregion

    //region Costruttore
    private JoinedMoviesController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, JoinedMoviesActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    //endregion

    //region getInstance() per il pattern singleton
    public static JoinedMoviesController getJoinedMoviesControllerInstance() {
        if(instance == null)
            instance = new JoinedMoviesController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setJoinedMoviesActivity(@NotNull JoinedMoviesActivity joinedMoviesActivity) {
        this.joinedMoviesActivity = joinedMoviesActivity;
    }
    //endregion

    public AdapterView.OnItemSelectedListener getJoinedMoviesActivitySpinnerListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] spinnerArray = joinedMoviesActivity.getResources().getStringArray(R.array.movies_list_tag);
                initializeActivityMovies(parent.getItemAtPosition(position).toString().equals(spinnerArray[0])); //"Preferiti"
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    //TODO: da modificare con film reali delle liste degli utenti dopo aver completato applicativo server
    public void initializeActivityMovies(boolean isFavourites) {
        Random rd = new Random();
        boolean tmp = rd.nextBoolean();

        if(!tmp) { //if(!listaVuota)
            if(joinedMoviesActivity.areMoviesHidden()) {
                joinedMoviesActivity.setMoviesVisibility(true);
                joinedMoviesActivity.setEmptyMovieListTextViewVisibility(false);
            }

            Thread t = new Thread(()-> {
                TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(joinedMoviesActivity.getResources().getString(R.string.themoviedb_api_key)));
                MovieResultsPage popular = tmdbMovies.getPopularMovies("it", 1);
                ArrayList<String> upcomingTitles = new ArrayList<>(),
                        upcomingImagesUrl = new ArrayList<>();
                ArrayList<Runnable> upcomingMoviesCardViewListeners = new ArrayList<>();

                initializeListsForJoinedMoviesAdapter(popular, upcomingTitles,
                        upcomingImagesUrl, upcomingMoviesCardViewListeners);

                actualAdapter = getJoinedMoviesRecyclerViewAdapter
                        (popular, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners);

                if(actualAdapter != null && actualAdapter.getItemCount() > 0) {
                    joinedMoviesActivity.setJoinedMoviesRecyclerView(actualAdapter);
                    joinedMoviesActivity.hideProgressBar();
                }
                else {
                    joinedMoviesActivity.setMoviesVisibility(false);
                    joinedMoviesActivity.setEmptyMovieListTextViewVisibility(true);
                }
            });
            t.start();

            try {
                t.join();
            }catch(InterruptedException ignore) {}
        }
        else {
            joinedMoviesActivity.setMoviesVisibility(false);
            joinedMoviesActivity.setEmptyMovieListTextViewVisibility(true);
            joinedMoviesActivity.hideProgressBar();
        }
    }

    private void initializeListsForJoinedMoviesAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                       @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                       @NotNull ArrayList<Runnable> movieCardViewListeners) {
        for (MovieDb movie : movieResultsPage) {
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
            if(Utilities.checkNullActivityOrNoConnection(joinedMoviesActivity)) return;

            if(movie.getOverview() == null || movie.getOverview().equals("")) {
                Thread t = new Thread(()-> {
                    TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(joinedMoviesActivity.getResources().getString(R.string.themoviedb_api_key)));
                    movie.setOverview(tmdbMovies.getMovie(movie.getId(), "en").getOverview());
                });
                t.start();

                try {
                    t.join();
                }catch(InterruptedException ignore) {}
            }

            joinedMoviesActivity.showProgressBar();
            ShowDetailsMovieController.getShowDetailsMovieControllerInstance()
                    .start(joinedMoviesActivity, movie);
        };
    }

    @Nullable
    public HomeStyleMovieAdapter getJoinedMoviesRecyclerViewAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                                    @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                                    @NotNull ArrayList<Runnable> movieCardViewListeners) {
        if (movieResultsPage.getTotalResults() > 0) {
            HomeStyleMovieAdapter homeStyleMovieAdapter = new HomeStyleMovieAdapter(joinedMoviesActivity, titles,
                    moviesImagesUrl, movieCardViewListeners, null);

            homeStyleMovieAdapter.setHasStableIds(true);

            return homeStyleMovieAdapter;
        }
        else
            return null;
    }

    //Restituisce un listener le icone della toolbar in JoinedMoviesActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(joinedMoviesActivity)) return;

            if(itemId == android.R.id.home) {
                joinedMoviesActivity.finish();
                joinedMoviesActivity.overridePendingTransition
                        (android.R.anim.fade_in,android.R.anim.fade_out);
            }
        };
    }

    public void hideJoinedMoviesProgressBar() {
        if(joinedMoviesActivity != null)
            joinedMoviesActivity.hideProgressBar();
    }
}
