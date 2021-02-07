package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

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

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.tools.ApiUrl;
import info.movito.themoviedbapi.tools.RequestMethod;
import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.ListaFilmDB;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.JoinedMoviesActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeStyleMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

import static info.movito.themoviedbapi.TmdbMovies.TMDB_METHOD_MOVIE;

public class JoinedMoviesController {
    //region Attributi
    private static JoinedMoviesController instance;
    private JoinedMoviesActivity joinedMoviesActivity;
    private HomeStyleMovieAdapter actualAdapter;
    private UserDB actualFriendUser;
    private static final String dbPath = BuildConfig.DB_PATH,
                                TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    //endregion

    //region Costruttore
    private JoinedMoviesController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity, UserDB actualFriendUser) {
        Intent intent = new Intent(activity, JoinedMoviesActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        this.actualFriendUser = actualFriendUser;
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

    public void initializeActivityMovies(boolean isFavourites) {
        Thread t = new Thread(()-> {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            String methodToRetrieveList = (isFavourites ? "getPreferitiByPossessore" : "getDaVedereByPossessore");
            String url = dbPath + "ListaFilm/" + methodToRetrieveList + "/{FK_Possessore}";

            String loggedUserEmail = User.getLoggedUser(joinedMoviesActivity).getEmail(),
                   friendUserEmail = actualFriendUser.getEmail();

            try {
                //Recupero delle liste film
                ListaFilmDB loggedUserMoviesList = restTemplate.getForObject(url, ListaFilmDB.class, loggedUserEmail);
                ListaFilmDB friendUserMoviesList = restTemplate.getForObject(url, ListaFilmDB.class, friendUserEmail);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<ListaFilmDB> requestUserListEntity = new HttpEntity<>(loggedUserMoviesList, headers);
                HttpEntity<ListaFilmDB> requestFriendListEntity = new HttpEntity<>(friendUserMoviesList, headers);

                //Recupero dei film delle liste
                url = dbPath + "ListaFilm/getAll";
                ResponseEntity<List<Long>> userMoviesIdsResponse = restTemplate.exchange
                        (url, HttpMethod.POST, requestUserListEntity, new ParameterizedTypeReference<List<Long>>() {});
                ResponseEntity<List<Long>> friendMoviesIdsResponse = restTemplate.exchange
                        (url, HttpMethod.POST, requestFriendListEntity, new ParameterizedTypeReference<List<Long>>() {});

                List<Long> userMoviesIds = userMoviesIdsResponse.getBody(),
                        friendMoviesIds = friendMoviesIdsResponse.getBody();

                if(userMoviesIds == null || friendMoviesIds == null) {
                    Utilities.stampaToast(joinedMoviesActivity, "Si è verificato un errore, riprova più tardi.");
                    return;
                }

                if(userMoviesIds.size() > 0 && friendMoviesIds.size() > 0) {
                    if(joinedMoviesActivity.areMoviesHidden()) {
                        joinedMoviesActivity.setMoviesVisibility(true);
                        joinedMoviesActivity.setEmptyMovieListTextViewVisibility(false);
                    }

                    userMoviesIds.retainAll(friendMoviesIds); //Ora userMoviesIds corrisponde all'intersezione delle due liste

                    ArrayList<String> titles = new ArrayList<>(),
                                      imagesUrl = new ArrayList<>();
                    ArrayList<Runnable> moviesCardViewListeners = new ArrayList<>();

                    TmdbApi tmdbApi  = new TmdbApi(TMDB_API_KEY);

                    initializeListsForJoinedMoviesAdapter(tmdbApi, userMoviesIds, titles,
                            imagesUrl, moviesCardViewListeners);

                    actualAdapter = getJoinedMoviesRecyclerViewAdapter
                            (userMoviesIds, titles, imagesUrl, moviesCardViewListeners);

                    if(actualAdapter != null && actualAdapter.getItemCount() > 0) {
                        joinedMoviesActivity.setJoinedMoviesRecyclerView(actualAdapter);
                        joinedMoviesActivity.hideProgressBar();
                    }
                    else {
                        joinedMoviesActivity.setMoviesVisibility(false);
                        joinedMoviesActivity.setEmptyMovieListTextViewVisibility(true);
                    }
                }
                else {
                    joinedMoviesActivity.setMoviesVisibility(false);
                    joinedMoviesActivity.setEmptyMovieListTextViewVisibility(true);
                    joinedMoviesActivity.hideProgressBar();
                }
            }catch (Exception e) {
                Utilities.stampaToast(joinedMoviesActivity, "Si è verificato un errore, riprova più tardi.");
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException ignore) {}
    }

    private void initializeListsForJoinedMoviesAdapter(@NotNull TmdbApi tmdbApi, @NotNull List<Long> moviesIds,
                                                       @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                       @NotNull ArrayList<Runnable> movieCardViewListeners) {
        TmdbMovies tmdbMovies = tmdbApi.getMovies();
        for (Long movieId : moviesIds) {
            MovieDb movie = tmdbMovies.getMovie(movieId.intValue(), "it");

            movieCardViewListeners.add(getMovieCardViewListener(movie));

            if(movie.getTitle() != null)
                titles.add(movie.getTitle());
            else
                titles.add(movie.getOriginalTitle());

            moviesImagesUrl.add(movie.getPosterPath());

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
        }
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getMovieCardViewListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(joinedMoviesActivity)) return;

            if(movie.getOverview() == null || movie.getOverview().equals("")) {
                Thread t = new Thread(()-> {
                    TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(TMDB_API_KEY));
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
    public HomeStyleMovieAdapter getJoinedMoviesRecyclerViewAdapter(@NotNull List<Long> moviesIds, @NotNull ArrayList<String> titles,
                                                                    @NotNull ArrayList<String> moviesImagesUrl,
                                                                    @NotNull ArrayList<Runnable> movieCardViewListeners) {
        if (moviesIds.size() > 0) {
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
