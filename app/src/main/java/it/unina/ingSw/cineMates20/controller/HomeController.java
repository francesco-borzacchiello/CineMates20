package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;

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
import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.adapter.HomeMovieAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;
public class HomeController {

    //region Attributi
    private static HomeController instance;
    private HomeActivity homeActivity;
    //endregion

    //region Costruttore
    private HomeController() {}
    //endregion

    //region Lancio dell'Activity
    public void start(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class);
        activity.startActivity(intent);
    }
    //endregion

    //region getIstance() per il pattern singleton
    public static HomeController getHomeControllerInstance() {
        if(instance == null)
            instance = new HomeController();
        return instance;
    }
    //endregion

    //region Setter del riferimento all'Activity gestita da questo controller
    public void setHomeActivity(@NotNull HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }
    //endregion

    //Costruisce e setta gli adapter per i RecyclerView che andranno a mostrare i film sulla home
    public void setHomeActivityMovies() {
        TmdbMovies tmdbMovies = new TmdbMovies(new TmdbApi(homeActivity.getResources().getString(R.string.themoviedb_api_key)));
        MovieResultsPage upcoming = tmdbMovies.getUpcoming("it", 1, null);           //Prossime uscite
        MovieResultsPage nowPlaying = tmdbMovies.getNowPlayingMovies("it", 1, null); //Ora in sala
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

        initializeListsForHomeMovieAdapter(upcoming, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners);
        HomeMovieAdapter upcomingAdapter = getHomeMoviesRecyclerViewAdapter(upcoming, upcomingTitles, upcomingImagesUrl, upcomingMoviesCardViewListeners);
        if(upcomingAdapter != null)
            homeActivity.setUpcomingHomeMoviesRecyclerView(upcomingAdapter);


        initializeListsForHomeMovieAdapter(nowPlaying, nowPlayingTitles, nowPlayingImagesUrl, nowPlayingMoviesCardViewListeners);
        HomeMovieAdapter nowPlayingAdapter = getHomeMoviesRecyclerViewAdapter(nowPlaying, nowPlayingTitles, nowPlayingImagesUrl, nowPlayingMoviesCardViewListeners);
        if(nowPlayingAdapter != null)
            homeActivity.setNowPlayingHomeMoviesRecyclerView(nowPlayingAdapter);


        initializeListsForHomeMovieAdapter(popular, popularTitles, popularImagesUrl, popularMoviesCardViewListeners);
        HomeMovieAdapter popularAdapter = getHomeMoviesRecyclerViewAdapter(popular, popularTitles, popularImagesUrl, popularMoviesCardViewListeners);
        if(popularAdapter != null)
            homeActivity.setMostPopularHomeMoviesRecyclerView(popularAdapter);

        initializeListsForHomeMovieAdapter(topRated, topRatedTitles, topRatedImagesUrl, topRatedMoviesCardViewListeners);
        HomeMovieAdapter topRatedAdapter = getHomeMoviesRecyclerViewAdapter(topRated, topRatedTitles, topRatedImagesUrl, topRatedMoviesCardViewListeners);
        if(topRatedAdapter != null)
            homeActivity.setTopRatedHomeMoviesRecyclerView(topRatedAdapter);

        homeActivity.showHomeTextViews();
    }

    private void initializeListsForHomeMovieAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                    @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                    @NotNull ArrayList<Runnable> movieCardViewListeners) {
        for (MovieDb movie : movieResultsPage) {
            movieCardViewListeners.add(getMovieCardViewListener(movie));

            if(movie.getTitle() != null)
                titles.add(movie.getTitle());
            else
                titles.add(movie.getOriginalTitle());

            moviesImagesUrl.add(movie.getPosterPath());
        }
    }

    @Nullable
    private HomeMovieAdapter getHomeMoviesRecyclerViewAdapter(@NotNull MovieResultsPage movieResultsPage,
                                                              @NotNull ArrayList<String> titles, @NotNull ArrayList<String> moviesImagesUrl,
                                                              @NotNull ArrayList<Runnable> movieCardViewListeners) {
        if (movieResultsPage.getTotalResults() > 0) {
            HomeMovieAdapter homeMovieAdapter = new HomeMovieAdapter(homeActivity, titles,
                    moviesImagesUrl, movieCardViewListeners);

            homeMovieAdapter.setHasStableIds(true);

            return homeMovieAdapter;
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
                homeActivity.getDrawerLayout().openDrawer(GravityCompat.START);
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
                homeActivity.onResume();
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

            //TODO: aggiungere la gestione degli altri item del menu...
        };
    }

    public void resetHomeRecyclerViewPosition() {
        if(homeActivity != null)
            homeActivity.resetRecyclersViewPosition();
    }

    private void handleHomeMenuItem(@NotNull Activity activity) {
        if(activity.equals(homeActivity)) {
            homeActivity.onResume();
            homeActivity.getDrawerLayout().closeDrawer(GravityCompat.START);
        }
        else {
            Intent intent = new Intent(activity, HomeActivity.class);
            activity.runOnUiThread(() -> Utilities.resumeBottomBackStackActivity(intent));

            activity.startActivity(intent);
            activity.overridePendingTransition(0,0);
            activity.finish();
        }
        homeActivity.resetRecyclersViewPosition();
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
                LinearLayout ll = homeActivity.findViewById(R.id.linearLayoutHome);
                ll.setVisibility(View.VISIBLE);
                ConstraintLayout cl = homeActivity.findViewById(R.id.constraintLayoutHome);
                cl.setBackgroundColor(homeActivity.getResources().getColor(R.color.white));
            }
        };
    }

    public View.OnClickListener getOnSearchClickListener() {
        return (view) -> {
            if(view.getId() == R.id.searchItem) {
                LinearLayout ll = homeActivity.findViewById(R.id.linearLayoutHome);
                ll.setVisibility(View.INVISIBLE);
                ConstraintLayout cl = homeActivity.findViewById(R.id.constraintLayoutHome);
                cl.setBackgroundColor(homeActivity.getResources().getColor(R.color.lightGray));
            }
        };
    }
}
