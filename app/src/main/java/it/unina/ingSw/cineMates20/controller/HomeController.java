package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import com.amplifyframework.core.Amplify;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.tools.ApiUrl;
import info.movito.themoviedbapi.tools.RequestMethod;
import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.activity.FriendsActivity;
import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.activity.InformationActivity;
import it.unina.ingSw.cineMates20.view.activity.MoviesListActivity;
import it.unina.ingSw.cineMates20.view.activity.PersonalProfileActivity;
import it.unina.ingSw.cineMates20.view.activity.SearchFriendsActivity;
import it.unina.ingSw.cineMates20.view.activity.SearchMovieActivity;
import it.unina.ingSw.cineMates20.view.activity.SettingsActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeStyleMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

import static info.movito.themoviedbapi.TmdbMovies.TMDB_METHOD_MOVIE;

public class HomeController {

    //region Attributi
    private static HomeController instance;
    private HomeActivity homeActivity;
    private SearchMovieActivity searchMovieActivity;
    private FriendsActivity friendsActivity;
    private MoviesListActivity moviesListActivity;
    private PersonalProfileActivity personalProfileActivity;
    private SearchFriendsActivity searchFriendsActivity;
    private InformationActivity informationActivity;
    private SettingsActivity settingsActivity;
    private TmdbMovies tmdbMovies;
    private static final String TMDB_API_KEY = BuildConfig.TMDB_API_KEY;
    //endregion

    //region Costruttore
    private HomeController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Thread t = new Thread(()->
                tmdbMovies = new TmdbMovies(new TmdbApi(TMDB_API_KEY)));
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}
    }
    //endregion

    public void startFromLogin(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        Utilities.clearBackStack(intent);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Thread t = new Thread(()-> tmdbMovies = new TmdbMovies(new TmdbApi(TMDB_API_KEY)));
        t.start();

        try{
            t.join();
        }catch(InterruptedException ignore){}
    }

    //region getInstance() per il pattern singleton
    public static HomeController getHomeControllerInstance() {
        if(instance == null)
            instance = new HomeController();
        return instance;
    }
    //endregion

    //region Setter del riferimento alle Activity gestite da questo controller
    public void setHomeActivity(@NotNull HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    public void setSearchMovieActivity(@NotNull SearchMovieActivity searchMovieActivity) {
        this.searchMovieActivity = searchMovieActivity;
    }

    public void setFriendsActivity(@NotNull FriendsActivity friendsActivity) {
        this.friendsActivity = friendsActivity;
    }

    public void setMoviesListActivity(MoviesListActivity moviesListActivity) {
        this.moviesListActivity = moviesListActivity;
    }

    public void setUserProfileActivity(PersonalProfileActivity personalProfileActivity) {
        this.personalProfileActivity = personalProfileActivity;
    }

    public void setSearchFriendsActivity(SearchFriendsActivity searchFriendsActivity) {
        this.searchFriendsActivity = searchFriendsActivity;
    }

    public void setInformationActivity(InformationActivity informationActivity) {
        this.informationActivity = informationActivity;
    }

    public void setSettingsActivity(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }
    //endregion

    //Costruisce e setta gli adapter per i RecyclerView che andranno a mostrare i film sulla home
    public void setHomeActivityMovies() {
        Thread t = new Thread(()-> {
            if(tmdbMovies == null)
                tmdbMovies = new TmdbMovies(new TmdbApi(TMDB_API_KEY));

            MovieResultsPage upcomingUsa = tmdbMovies.getUpcoming("it", 1, "US");           //Prossime uscite USA
            MovieResultsPage upcomingIt = tmdbMovies.getUpcoming("it", 1, "IT");           //Prossime uscite Italia
            MovieResultsPage nowPlaying = tmdbMovies.getNowPlayingMovies("it", 1, "IT"); //Ora in sala
            MovieResultsPage popular = tmdbMovies.getPopularMovies("it", 1);                    //Di tendenza
            MovieResultsPage topRated = tmdbMovies.getTopRatedMovies("it",1);                   //I più votati

            //Nota: ad ogni adapter occorre un ArrayList diverso
            ArrayList<String> upcomingTitles = new ArrayList<>(),
                    nowPlayingTitles = new ArrayList<>(),
                    popularTitles = new ArrayList<>(),
                    topRatedTitles = new ArrayList<>(),
                    upcomingImagesUrl = new ArrayList<>(),
                    nowPlayingImagesUrl = new ArrayList<>(),
                    popularImagesUrl = new ArrayList<>(),
                    topRatedImagesUrl = new ArrayList<>();
            ArrayList<Runnable> upcomingMoviesCardViewListeners = new ArrayList<>(),
                    nowPlayingMoviesCardViewListeners = new ArrayList<>(),
                    popularMoviesCardViewListeners = new ArrayList<>(),
                    topRatedMoviesCardViewListeners = new ArrayList<>();

            initializeListsForHomeMovieAdapter(upcomingIt, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners);
            if(upcomingIt.getTotalResults() < 20) //Se sono stati trovati meno di 20 risultati per la regione italiana, si aggiungono quelli USA
                initializeListsForHomeMovieAdapter(upcomingUsa, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners);

            HomeStyleMovieAdapter upcomingAdapter = getHomeMoviesRecyclerViewAdapter
                    (upcomingIt.getTotalResults() + upcomingUsa.getTotalResults(),
                            upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners);
            if(upcomingAdapter != null)
                homeActivity.setUpcomingHomeMoviesRecyclerView(upcomingAdapter);

            
            initializeListsForHomeMovieAdapter(nowPlaying, nowPlayingTitles, nowPlayingImagesUrl, nowPlayingMoviesCardViewListeners);
            HomeStyleMovieAdapter nowPlayingAdapter = getHomeMoviesRecyclerViewAdapter
                    (nowPlaying.getTotalResults(), nowPlayingTitles, nowPlayingImagesUrl, nowPlayingMoviesCardViewListeners);
            if(nowPlayingAdapter != null)
                homeActivity.setNowPlayingHomeMoviesRecyclerView(nowPlayingAdapter);

            initializeListsForHomeMovieAdapter(popular, popularTitles, popularImagesUrl, popularMoviesCardViewListeners);
            HomeStyleMovieAdapter popularAdapter = getHomeMoviesRecyclerViewAdapter
                    (popular.getTotalResults(), popularTitles, popularImagesUrl, popularMoviesCardViewListeners);
            if(popularAdapter != null)
                homeActivity.setMostPopularHomeMoviesRecyclerView(popularAdapter);


            initializeListsForHomeMovieAdapter(topRated, topRatedTitles, topRatedImagesUrl, topRatedMoviesCardViewListeners);
            Collections.reverse(topRatedTitles); Collections.reverse(topRatedImagesUrl); Collections.reverse(topRatedMoviesCardViewListeners);

            HomeStyleMovieAdapter topRatedAdapter = getHomeMoviesRecyclerViewAdapter
                    (topRated.getTotalResults(), topRatedTitles, topRatedImagesUrl, topRatedMoviesCardViewListeners);
            if(topRatedAdapter != null)
                homeActivity.setTopRatedHomeMoviesRecyclerView(topRatedAdapter);

            homeActivity.showHomeTextViews();
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}
    }

    public void initializeListsForHomeMovieAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                   @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                   @NotNull ArrayList<Runnable> movieCardViewListeners) {
        TmdbApi tmdbApi = new TmdbApi(TMDB_API_KEY);

        for (MovieDb movie : movieResultsPage) {
            movieCardViewListeners.add(getMovieCardViewListener(movie));

            //Verifica se il film dispone di traduzione italiana
            new Thread(()-> {
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
            }).start();

            if(movie.getTitle() != null)
                titles.add(movie.getTitle());
            else {
                movie.setTitle(movie.getOriginalTitle());
                titles.add(movie.getOriginalTitle());
            }

            moviesImagesUrl.add(movie.getPosterPath());

            if(titles.size() > 19)
                break;
        }

    }

    @Nullable
    public HomeStyleMovieAdapter getHomeMoviesRecyclerViewAdapter(int totalResults,
                                                                  @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                                  @NotNull ArrayList<Runnable> movieCardViewListeners) {
        if (totalResults > 0) {
            HomeStyleMovieAdapter homeStyleMovieAdapter = new HomeStyleMovieAdapter(homeActivity, titles,
                    moviesImagesUrl, movieCardViewListeners, null);

            homeStyleMovieAdapter.setHasStableIds(true);

            return homeStyleMovieAdapter;
        }
        else
            return null;
    }

    @NotNull
    @Contract(pure = true)
    private Runnable getMovieCardViewListener(MovieDb movie) {
        return ()-> {
            if(Utilities.checkNullActivityOrNoConnection(homeActivity)) return;

            homeActivity.showMovieProgressBar();
            ShowDetailsMovieController.getShowDetailsMovieControllerInstance()
                    .start(homeActivity, movie);
        };
    }

    public void hideHomeMovieProgressBar() {
        if(homeActivity != null)
            homeActivity.hideMovieProgressBar();
    }

    //Restituisce un listener le icone della toolbar in HomeActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            if(itemId == android.R.id.home)
                homeActivity.openDrawerLayout();
            else if(itemId == R.id.notificationItem &&
                    !Utilities.checkNullActivityOrNoConnection(homeActivity)) {
                NotificationController.getNotificationControllerInstance().start(homeActivity);
            }
        };
    }

    public SearchView.OnQueryTextListener getSearchViewOnQueryTextListener() {
        return new SearchView.OnQueryTextListener() {
            //Questo metodo non viene invocato se la query è vuota
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleOnSearchPressed(query);
                homeActivity.keepSearchViewExpanded();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };
    }

    private void handleOnSearchPressed(String query) {
        if(Utilities.checkNullActivityOrNoConnection(homeActivity)) return;

        SearchMovieController.getSearchMovieControllerInstance().start(homeActivity, query);
    }

    //Restituisce un listener per una delle icone del NavigationView
    public Runnable getNavigationViewOnOptionsItemSelected(Activity activity, int itemId) {
        return () -> {
            if(Utilities.checkNullActivityOrNoConnection(activity)) return;

            if(itemId == R.id.menuLogout)
                handleLogoutMenuItem(activity);
            else if(itemId == R.id.menuHome)
                handleHomeMenuItem(activity);
            else if(itemId == R.id.menuFriends)
                handleFriendsMenuItem(activity);
            else if(itemId == R.id.menuFavourites)
                handleListMenuItem(activity, true);
            else if(itemId == R.id.menuToWatch)
                handleListMenuItem(activity, false);
            else if(itemId == R.id.menuProfile)
                handleProfileMenuItem(activity);
            else if(itemId == R.id.menuInfo)
                handleInformationMenuItem(activity);
            else //itemId == R.id.menuSettings
                handleSettingsMenuItem(activity);
        };
    }

    private void handleSettingsMenuItem(Activity activity) {
        closeActivityNavigationView(activity);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                SettingsController.getSettingsControllerInstance().start(activity), 240);
    }

    private void handleInformationMenuItem(Activity activity) {
        closeActivityNavigationView(activity);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            activity.startActivity(new Intent(activity, InformationActivity.class));
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 240);
    }

    private void handleProfileMenuItem(Activity activity) {
        closeActivityNavigationView(activity);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
            PersonalProfileController.getPersonalProfileControllerInstance().start(activity), 240);
    }

    private void handleListMenuItem(Activity activity, boolean isFavourites) {
        if(activity != null) {
            closeActivityNavigationView(activity);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                MoviesListsController.getMoviesListControllerInstance()
                        .start(activity, isFavourites);

                if(activity.equals(moviesListActivity))
                    activity.finish();
            }, 240);
        }
    }

    private void handleFriendsMenuItem(Activity activity) {
        if(activity != null) {
            closeActivityNavigationView(activity);

            if(!activity.equals(friendsActivity))
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        FriendsController.getFriendsControllerInstance()
                        .start(activity), 240);
        }
    }

    public void resetHomeRecyclerViewPosition() {
        if(homeActivity != null)
            homeActivity.resetRecyclersViewPosition();
    }

    private void handleHomeMenuItem(@NotNull Activity activity) {
        if(activity.equals(homeActivity)) {
            homeActivity.onResume();
            homeActivity.closeDrawerLayout();
        }
        else {
            closeActivityNavigationView(activity);

            Intent intent = new Intent(activity, HomeActivity.class);
            activity.runOnUiThread(() -> Utilities.resumeBottomBackStackActivity(intent));

            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            activity.finish();
        }
        homeActivity.resetRecyclersViewPosition();
    }

    //region Gestore d'evento per il logout
    private void handleLogoutMenuItem(Activity activity) {
        if(activity != null) {
            //Chiede all'utente se è sicuro di volersi disconnettersi (logout), in caso positivo si torna alla login
            AlertDialog alertDialog = new AlertDialog.Builder(activity)
                    .setMessage("Sei sicuro di volerti disconnettere?")
                    .setCancelable(false)
                    .setPositiveButton("Si", (dialog, id) -> {
                        if (Utilities.checkNullActivityOrNoConnection(activity)) return;
                        logOut(activity);
                    })
                    .setNegativeButton("No", null).show();

            alertDialog.setOnKeyListener((arg0, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    alertDialog.dismiss();
                }
                return true;
            });
        }
    }

    //region Logica del logout
    private void logOut(Activity activity) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(homeActivity);
        if (acct != null) { //Logout da google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(homeActivity, gso);
            mGoogleSignInClient.signOut();
            backToLogin(activity);
        }
        else if(AccessToken.getCurrentAccessToken() != null) { //Logout da facebook
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE,
                    graphResponse -> LoginManager.getInstance().logOut()).executeAndWait();

            backToLogin(activity);
        }
        else { //Logout da Cognito
            Amplify.Auth.signOut(
                    () -> backToLogin(activity),
                    error -> homeActivity.runOnUiThread(() -> Utilities.stampaToast(homeActivity, "Si è verificato un errore.\nRiprova tra qualche minuto."))
            );
        }
        User.deleteUserInstance();
    }

    private void backToLogin(Activity activity) {
        closeActivityNavigationView(activity);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            //Mostra schermata home con un intent, passando inizialmente homeActivity come parent e poi distruggendo tutte le activity create
            Intent intent = new Intent(activity, EntryPoint.class);
            activity.runOnUiThread(() -> Utilities.stampaToast(activity, "Logout effettuato."));
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            activity.finish();
        }, 240);
    }

    //endregion
    //endregion

    private void closeActivityNavigationView(@NotNull Activity activity) {
        if(activity.equals(homeActivity))
            homeActivity.closeDrawerLayout();
        else if(activity.equals(searchMovieActivity))
            searchMovieActivity.closeDrawerLayout();
        else if(activity.equals(friendsActivity))
            friendsActivity.closeDrawerLayout();
        else if(activity.equals(moviesListActivity))
            moviesListActivity.closeDrawerLayout();
        else if(activity.equals(personalProfileActivity))
            personalProfileActivity.closeDrawerLayout();
        else if(activity.equals(searchFriendsActivity))
            searchFriendsActivity.closeDrawerLayout();
        else if(activity.equals(informationActivity))
            informationActivity.closeDrawerLayout();
        else
            settingsActivity.closeDrawerLayout();
    }

    //Collassa la searchview in caso di annullamento ricerca
    public View.OnFocusChangeListener getSearchViewOnQueryTextFocusChangeListener() {
        return (view, queryTextFocused) -> {
            if (!queryTextFocused) {
                homeActivity.onResume();
                homeActivity.setLayoutsForHome(false);
            }
        };
    }

    public View.OnClickListener getOnSearchClickListener() {
        return (view) -> {
            if(view.getId() == R.id.searchItem)
                homeActivity.setLayoutsForHome(true);
        };
    }
}
