package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

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
import java.util.SortedSet;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.tools.ApiUrl;
import info.movito.themoviedbapi.tools.RequestMethod;
import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.ListaFilmDB;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.activity.MoviesListActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeStyleMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

import static info.movito.themoviedbapi.TmdbMovies.TMDB_METHOD_MOVIE;

public class MoviesListsController {
    //region Attributi
    private static MoviesListsController instance;
    private MoviesListActivity moviesListActivity;
    private HomeStyleMovieAdapter actualAdapter;
    private static final String dbPath = BuildConfig.DB_PATH,
                                TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    //endregion

    //region Costruttore
    private MoviesListsController() {}
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
    public static MoviesListsController getMoviesListControllerInstance() {
        if(instance == null)
            instance = new MoviesListsController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setMoviesListActivity(@NonNull MoviesListActivity moviesListActivity) {
        this.moviesListActivity = moviesListActivity;
    }
    //endregion

    public OnItemSelectedListener getMoviesListActivitySpinnerListener() {
        return new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] spinnerArray = moviesListActivity.getResources().getStringArray(R.array.movies_list_tag);
                Thread t = new Thread(()-> {
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                    String methodToRetrieveList = (parent.getItemAtPosition(position).toString().equals(spinnerArray[0]) ? "getPreferitiByPossessore" : "getDaVedereByPossessore");
                    String url = dbPath + "ListaFilm/" + methodToRetrieveList + "/{FK_Possessore}";

                    String email = User.getLoggedUser(moviesListActivity).getEmail();

                    ListaFilmDB listaFilm = restTemplate.getForObject(url, ListaFilmDB.class, email);
                    initializeActivityMovies(listaFilm);
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

    public void initializeActivityMovies(ListaFilmDB list) {
        Thread t = new Thread(()-> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            HttpEntity<ListaFilmDB> requestListEntity = new HttpEntity<>(list, headers);

            String url = dbPath + "ListaFilm/getAll";
            ResponseEntity<List<Long>> moviesIds = restTemplate.exchange
                    (url, HttpMethod.POST, requestListEntity, new ParameterizedTypeReference<List<Long>>() {});

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
    }

    public void aggiungiFilmAPreferiti(@NotNull MovieDb movie, Activity activity) {
        getListenerForManageListOfFavourites(movie.getId(), "addFilmToListaFilm", activity);
    }

    public void rimuoviFilmDaPreferiti(@NotNull MovieDb movie, Activity activity) {
        getListenerForManageListOfFavourites(movie.getId(), "removeFilmFromListaFilm", activity);
    }

    public void aggiungiFilmInDaVedere(@NotNull MovieDb movie, Activity activity) {
        getListenerForManageListToWatch(movie.getId(), "addFilmToListaFilm", activity);
    }

    public void rimuoviFilmDaVedere(@NotNull MovieDb movie, Activity activity) {
        getListenerForManageListToWatch(movie.getId(), "removeFilmFromListaFilm", activity);
    }

    private void getListenerForManageListOfFavourites(int idMovie, String methodForEditingList, Activity activity) {
        getListenerForSendRequestsToTheServer(idMovie, "getPreferitiByPossessore", methodForEditingList, activity);
    }

    private void getListenerForManageListToWatch(int idMovie, String methodForEditingList, Activity activity) {
        getListenerForSendRequestsToTheServer(idMovie, "getDaVedereByPossessore", methodForEditingList, activity);
    }

    private void getListenerForSendRequestsToTheServer(int idFilm, String methodToRetrieveList, String methodForEditingList, Activity activity) {
        Thread t = new Thread(()-> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            String url = dbPath + "ListaFilm/" + methodToRetrieveList + "/{FK_Possessore}";

            String email = User.getLoggedUser(activity).getEmail();

            ListaFilmDB listaFilm = restTemplate.getForObject(url, ListaFilmDB.class, email);
            HttpEntity<ListaFilmDB> requestListaPreferitiEntity = new HttpEntity<>(listaFilm, headers);

            url = dbPath + "ListaFilm/" + methodForEditingList + "/{FK_Film}";
            restTemplate.postForEntity(url, requestListaPreferitiEntity, ListaFilmDB.class, idFilm);
        });

        t.start();
        try {
            t.join();
        }catch (InterruptedException ignore) {}
    }

    public void showEmptyMovieList() {
        moviesListActivity.setMoviesVisibility(false);
        moviesListActivity.setEmptyMovieListTextViewVisibility(true);
    }

    private void initializeListsForMoviesListAdapter(@NotNull List<Long> moviesIds) {
        TmdbApi tmdbApi = new TmdbApi(TMDB_API_KEY);
        TmdbMovies tmdbMovies = tmdbApi.getMovies();

        ArrayList<String> titles = new ArrayList<>(),
                          imagesUrl = new ArrayList<>();
        ArrayList<Runnable> moviesCardViewListeners = new ArrayList<>();

        for (Long id : moviesIds) {
            MovieDb movie = tmdbMovies.getMovie(id.intValue(), "it");
            moviesCardViewListeners.add(getMovieCardViewListener(movie));

            //Verifica se il film dispone di traduzione italiana
            Thread t = new Thread(()-> {
                //L'aggiornamento dei titoli avverrà a pagina film aperta
                String webpage = tmdbApi.requestWebPage(new ApiUrl(TMDB_METHOD_MOVIE, movie.getId(),
                        TmdbMovies.MovieMethod.translations), null, RequestMethod.GET);

                if (!webpage.contains("Italiano")) {
                    MovieDb engMovie = tmdbMovies.getMovie(movie.getId(), "en");
                    //Se non disponibile traduzione italiana, inserisci titolo inglese
                    movie.setTitle(engMovie.getTitle());

                    if (movie.getOverview() == null || movie.getOverview().equals(""))
                        movie.setOverview(engMovie.getOverview());
                }
            });
            t.start();

            //L'aggiornamento dei titoli avverrà immediatamente
            if(moviesIds.size() < 15) {
                try {
                    t.join();
                }catch(InterruptedException ignore){}
            } //else L'aggiornamento dei titoli avverrà a pagina film aperta

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

    public OnMenuItemClickListener getOnMenuItemClickListener() {
        return item -> {
            if(Utilities.checkNullActivityOrNoConnection(moviesListActivity)) return true;

            if(actualAdapter.isDeleteEnabled()) {
                deleteMovieFromDatabase(actualAdapter.getDeleteList());
                actualAdapter.deleteSelectedItem();

                moviesListActivity.getMoviesRecyclerView().getRecycledViewPool().clear();
            }
            updateAllMoviesCheckBoxesVisibility();
            return true;
        };
    }

    private void deleteMovieFromDatabase(@NotNull SortedSet<Long> deleteList) {
        new Thread(() -> {
            TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(TMDB_API_KEY));
            String[] spinnerArray = moviesListActivity.getResources().getStringArray(R.array.movies_list_tag);

            for(Long id : deleteList)
                if(moviesListActivity.getSelectedSpinnerItem().equals(spinnerArray[0]))
                    rimuoviFilmDaPreferiti(tmdbMovies.getMovie(id.intValue(), "it"), moviesListActivity);
                else
                    rimuoviFilmDaVedere(tmdbMovies.getMovie(id.intValue(), "it"), moviesListActivity);
        }).start();
    }

    public void resetAllMoviesCheckBoxes() {
        actualAdapter.resetAllMoviesCheckBoxes();
    }
}