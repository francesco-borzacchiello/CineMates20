package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import com.amplifyframework.core.Amplify;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import it.unina.ingSw.cineMates20.EntryPoint;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.FriendsActivity;
import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.activity.MoviesListActivity;
import it.unina.ingSw.cineMates20.view.activity.SearchMovieActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeStyleMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;
public class HomeController {

    //region Attributi
    private static HomeController instance;
    private HomeActivity homeActivity;
    private SearchMovieActivity searchMovieActivity;
    private FriendsActivity friendsActivity;
    private MoviesListActivity moviesListActivity;
    private TmdbMovies tmdbMovies;
    //endregion

    //region Costruttore
    private HomeController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
        tmdbMovies = new TmdbMovies(new TmdbApi(activity.getResources().getString(R.string.themoviedb_api_key)));
    }
    //endregion

    //region getInstance() per il pattern singleton
    public static HomeController getHomeControllerInstance() {
        if(instance == null)
            instance = new HomeController();
        return instance;
    }
    //endregion

    //region Setter del riferimento alle Activity gestite da questo controller
    public void setHomeActivity(@NotNull HomeActivity homeActivity) {
        if(this.homeActivity == null)
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
    //endregion

    //Costruisce e setta gli adapter per i RecyclerView che andranno a mostrare i film sulla home
    public void setHomeActivityMovies() {
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
        HomeStyleMovieAdapter topRatedAdapter = getHomeMoviesRecyclerViewAdapter
                (topRated.getTotalResults(), topRatedTitles, topRatedImagesUrl, topRatedMoviesCardViewListeners);
        if(topRatedAdapter != null)
            homeActivity.setTopRatedHomeMoviesRecyclerView(topRatedAdapter);

        homeActivity.showHomeTextViews();
    }

    public void initializeListsForHomeMovieAdapter(@NotNull MovieResultsPage movieResultsPage,
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
                    .start(homeActivity, movie, "HomeActivity");
        };
    }

    public void hideHomeMovieProgressBar() {
        if(homeActivity != null)
            homeActivity.hideMovieProgressBar();
    }

    //Restituisce un listener le icone della toolbar in HomeActivity
    public Runnable getOnOptionsItemSelected(int itemId) {
        return () -> {
            //TODO: spostare negli handler concreti : if(Utilities.checkNullActivityOrNoConnection(homeActivity)) return;

            //Nota: non è possibile fare switch case a causa del fatto che le risorse in id non sono più final
            if(itemId == android.R.id.home)
                homeActivity.openDrawerLayout();
            //else if(itemId == ...)

            //TODO: aggiungere la gestione degli altri item del menu, come la search (invio richiesta a themoviedb)...
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

            //TODO: aggiungere la gestione degli altri item del menu...
        };
    }

    private void handleListMenuItem(Activity activity, boolean isFavourites) {
        if(activity != null) {
            closeActivityNavigationView(activity);

            new Handler().postDelayed(() -> {
                MoviesListController.getMoviesListControllerInstance()
                        .start(activity, isFavourites);

                if(activity.equals(moviesListActivity))
                    activity.finish();
            }, 250);
        }
    }

    private void handleFriendsMenuItem(Activity activity) {
        if(activity != null) {
            closeActivityNavigationView(activity);

            new Handler().postDelayed(() -> FriendsController.getFriendsControllerInstance()
                    .start(activity), 250);
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

    private void closeActivityNavigationView(@NotNull Activity activity) {
        if(activity.equals(homeActivity))
            homeActivity.closeDrawerLayout();
        else if(activity.equals(searchMovieActivity))
            searchMovieActivity.closeDrawerLayout();
        else if(activity.equals(friendsActivity)) {
            friendsActivity.closeDrawerLayout();
        }
        else if(activity.equals(moviesListActivity)) {
            moviesListActivity.closeDrawerLayout();
        }
    }

    //region Gestore d'evento per il logout
    private void handleLogoutMenuItem(Activity activity) {
        if(activity != null)
            //Chiede all'utente se è sicuro di volersi disconnettersi (logout), in caso positivo si torna alla login
            new AlertDialog.Builder(activity)
                    .setMessage("Sei sicuro di volerti disconnettere?")
                    .setCancelable(false)
                    .setPositiveButton("Si", (dialog, id) -> {
                        if (Utilities.checkNullActivityOrNoConnection(activity)) return;
                        logOut();
                    })
                    .setNegativeButton("No", null)
                    .show();
    }

    //region Logica del logout
    private void logOut() {
        Amplify.Auth.signOut(
                () -> {
                    //Mostra schermata home con un intent, passando inizialmente homeActivity come parent e poi distruggendo tutte le activity create
                    Intent intent = new Intent(homeActivity, EntryPoint.class);
                    homeActivity.runOnUiThread(() -> Utilities.clearBackStack(intent));
                    homeActivity.startActivity(intent);
                    homeActivity.finish();
                    homeActivity.runOnUiThread(() -> Utilities.stampaToast(homeActivity, "Logout effettuato."));
                },
                error -> homeActivity.runOnUiThread(() -> Utilities.stampaToast(homeActivity, "Si è verificato un errore.\nRiprova tra qualche minuto."))
        );
    }

    //endregion
    //endregion

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
