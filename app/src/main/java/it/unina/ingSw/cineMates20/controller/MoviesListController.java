package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.ListaFilmDB;
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

    public AdapterView.OnItemSelectedListener getMoviesListActivitySpinnerListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] spinnerArray = moviesListActivity.getResources().getStringArray(R.array.movies_list_tag);
                Thread t = new Thread(()-> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                    String url;
                    String methodToRetrieveList = (parent.getItemAtPosition(position).toString().equals(spinnerArray[0]) ? "getPreferitiByPossessore" : "getDaVedereByPossessore");
                    url = moviesListActivity.getResources().getString(R.string.db_path) + "ListaFilm/" + methodToRetrieveList + "/{FK_Possessore}";

                    List<String> info = Utilities.getCurrentUserInformations(moviesListActivity);
                    String email = info.get(3);

                    ListaFilmDB listaFilmPreferiti = restTemplate.getForObject(url, ListaFilmDB.class, email);
                    initializeActivityMovies(listaFilmPreferiti);
                });

                t.start();

                try {
                    t.join();
                } catch (InterruptedException ignore) {}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    }

    //TODO: da modificare con film reali della lista dell'utente dopo aver completato applicativo server
    public void initializeActivityMovies(ListaFilmDB list) {
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
        Thread t = new Thread(()-> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            HttpEntity<ListaFilmDB> requestListEntity = new HttpEntity<>(list, headers);

            String url = moviesListActivity.getResources().getString(R.string.db_path) + "ListaFilm/getAll";
            ResponseEntity<List<Long>> moviesIds = restTemplate.exchange(url, HttpMethod.POST, requestListEntity,new ParameterizedTypeReference<List<Long>>() {});

            if(!moviesIds.getBody().isEmpty()) {
                if(moviesListActivity.areMoviesHidden()) {
                    moviesListActivity.setMoviesVisibility(true);
                    moviesListActivity.setEmptyMovieListTextViewVisibility(false);
                }

                initializeListsForMoviesListAdapter(moviesIds.getBody());
            } else {
                moviesListActivity.setMoviesVisibility(false);
                moviesListActivity.setEmptyMovieListTextViewVisibility(true);
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException ignore) {}

        //TODO: rimuovere questo codice temporaneo successivamente e sostituirlo con quello sopra
        /*Random rd = new Random();
        boolean tmp = rd.nextBoolean();

        if(!tmp) { //if(!listaVuota)
            if(moviesListActivity.areMoviesHidden()) {
                moviesListActivity.setMoviesVisibility(true);
                moviesListActivity.setEmptyMovieListTextViewVisibility(false);
            }

            Thread t = new Thread(()-> {
                TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(moviesListActivity.getResources().getString(R.string.themoviedb_api_key)));
                MovieResultsPage upcomingUsa = tmdbMovies.getUpcoming("it", 1, "US");
                ArrayList<String> upcomingTitles = new ArrayList<>(),
                        upcomingImagesUrl = new ArrayList<>();
                ArrayList<Runnable> upcomingMoviesCardViewListeners = new ArrayList<>();
                ArrayList<Integer> moviesIds = new ArrayList<>();
                //TODO: tmdbMovies.getMovie(1, "it");
                initializeListsForMoviesListAdapter(upcomingUsa, upcomingTitles,
                        upcomingImagesUrl, upcomingMoviesCardViewListeners, moviesIds);

                actualAdapter = getMoviesListRecyclerViewAdapter
                        (upcomingUsa, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners, moviesIds);

                if(actualAdapter != null)
                    moviesListActivity.setMoviesListRecyclerView(actualAdapter);
            });

            t.start();

            try {
                t.join();
            }catch(InterruptedException ignore) {}
        }
        else {
            moviesListActivity.setMoviesVisibility(false);
            moviesListActivity.setEmptyMovieListTextViewVisibility(true);
        }*/

    }

    public void showEmptyMovieList() {
        moviesListActivity.setMoviesVisibility(false);
        moviesListActivity.setEmptyMovieListTextViewVisibility(true);
    }

    private void initializeListsForMoviesListAdapter(@NotNull List<Long> moviesIds) {
        TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(moviesListActivity.getResources().getString(R.string.themoviedb_api_key)));

        ArrayList<String> titles = new ArrayList<>(),
                          imagesUrl = new ArrayList<>();
        ArrayList<Runnable> moviesCardViewListeners = new ArrayList<>();

        for (Long id : moviesIds) {
            MovieDb movie = tmdbMovies.getMovie(id.intValue(), "it");
            moviesCardViewListeners.add(getMovieCardViewListener(movie));

            if(movie.getTitle() != null)
                titles.add(movie.getTitle());
            else
                titles.add(movie.getOriginalTitle());

            imagesUrl.add(movie.getPosterPath());
        }
        actualAdapter = getMoviesListRecyclerViewAdapter(moviesIds, titles, imagesUrl, moviesCardViewListeners);

        if(actualAdapter != null)
            moviesListActivity.setMoviesListRecyclerView(actualAdapter);
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getMovieCardViewListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(moviesListActivity)) return;

            moviesListActivity.showProgressBar();
            ShowDetailsMovieController.getShowDetailsMovieControllerInstance()
                    .start(moviesListActivity, movie);
        };
    }

    @Nullable
    public HomeStyleMovieAdapter getMoviesListRecyclerViewAdapter(@NotNull List<Long> moviesIds,
                                                                  @NotNull ArrayList<String> titles,
                                                                  @NotNull ArrayList<String> moviesImagesUrl,
                                                                  @NotNull ArrayList<Runnable> movieCardViewListeners) {
        if (moviesIds.size() > 0) {
            HomeStyleMovieAdapter homeStyleMovieAdapter = new HomeStyleMovieAdapter(moviesListActivity, titles,
                                                                                    moviesImagesUrl, movieCardViewListeners, moviesIds);

            homeStyleMovieAdapter.setHasStableIds(true);

            return homeStyleMovieAdapter;
        }
        else return null;
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
            moviesListActivity.hideProgressBar();
    }

    public RecyclerView getMoviesRecyclerView() {
        return moviesListActivity.getMoviesRecyclerView();
    }

    public void updateAllMoviesCheckBoxesVisibility() {
        actualAdapter.updateVisibility();
        actualAdapter.notifyDataSetChanged();
    }

    public boolean isDeleteEnabled() {
        if(actualAdapter == null) return false;
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
