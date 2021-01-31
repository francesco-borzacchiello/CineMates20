package it.unina.ingSw.cineMates20.controller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.Artwork;
import info.movito.themoviedbapi.model.ArtworkType;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.MovieImages;
import info.movito.themoviedbapi.model.ReleaseDate;
import info.movito.themoviedbapi.model.ReleaseInfo;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.model.ListaFilmDB;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.activity.HomeActivity;
import it.unina.ingSw.cineMates20.view.activity.JoinedMoviesActivity;
import it.unina.ingSw.cineMates20.view.activity.MoviesListActivity;
import it.unina.ingSw.cineMates20.view.activity.SearchMovieActivity;
import it.unina.ingSw.cineMates20.view.activity.ShowDetailsMovieActivity;
import it.unina.ingSw.cineMates20.view.adapter.ActorMovieAdapter;

public class ShowDetailsMovieController {

    private static ShowDetailsMovieController instance;
    private static TmdbMovies tmdbMovies;
    private ShowDetailsMovieActivity showDetailsMovieActivity;
    private MovieDb actualMovie;
    private String actualMovieKeyTrailer;
    private AppCompatActivity activityParent;
    private boolean isParentMoviesListActivity;

    private ShowDetailsMovieController() {}

    public static ShowDetailsMovieController getShowDetailsMovieControllerInstance() {
        if(instance == null)
            instance = new ShowDetailsMovieController();
        return instance;
    }

    public void start(AppCompatActivity activityParent, MovieDb movie) {
        isParentMoviesListActivity = activityParent instanceof MoviesListActivity;
        actualMovie = movie;

        Intent intent = new Intent(activityParent, ShowDetailsMovieActivity.class);
        //Utilizzato per comunicare a ShowDetailsMovieActivity se il suo chiamante non è SearchMovieActivity
        this.activityParent = activityParent;

        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public boolean parentIsMoviesListActivity() {
        return isParentMoviesListActivity;
    }

    public void setShowDetailsMovieActivity(@NotNull ShowDetailsMovieActivity activity) {
        showDetailsMovieActivity = activity;
        hideProgressBar();

        new Thread(()-> {
            if(tmdbMovies == null)
                tmdbMovies = new TmdbMovies(new TmdbApi(activity.getResources().getString(R.string.themoviedb_api_key)));

            List<Video> videos = tmdbMovies.getVideos(actualMovie.getId(), "it");

            if(videos.size() == 0) {
                videos = tmdbMovies.getVideos(actualMovie.getId(), "en");
                if(videos.size() == 0) {
                    actualMovieKeyTrailer = null;
                    return;
                }

                for(Video video: videos) {
                    if(video.getSite().equals("YouTube")) {
                        actualMovieKeyTrailer = video.getKey();
                        break;
                    }
                }
            } else {
                for(Video video: videos) {
                    if(video.getSite().equals("YouTube")) {
                        actualMovieKeyTrailer = video.getKey();
                        break;
                    }
                }
            }
        }).start();
    }

    public void initializeShowDetailsMovieActivity() {
        String title;
        if(actualMovie.getTitle() != null)
            title = actualMovie.getTitle();
        else {
            if(tmdbMovies == null)
                tmdbMovies = new TmdbMovies(new TmdbApi(showDetailsMovieActivity.getResources().getString(R.string.themoviedb_api_key)));
            title = tmdbMovies.getMovie(actualMovie.getId(), "en").getTitle();
            if(title == null)
                title = actualMovie.getOriginalTitle();
        }
        String prefissoDataDiUscita = showDetailsMovieActivity.getResources().getString(R.string.data_di_uscita),
               prefissoRegista = showDetailsMovieActivity.getResources().getString(R.string.regista),
               prefissoGenere = showDetailsMovieActivity.getResources().getString(R.string.genere),
               prefissoDurata = showDetailsMovieActivity.getResources().getString(R.string.durata),
               prefissoEtaMinima = showDetailsMovieActivity.getResources().getString(R.string.etaConsigliata);

        showDetailsMovieActivity.setBackgroundImageSlider(createListForBackgroundImage());

        initializeAdapterForMovieCast(actualMovie.getId());

        showDetailsMovieActivity.setMovieDetails(title, prefissoDataDiUscita +
                getEuropeanFormatMovieReleaseDate(actualMovie),
                prefissoRegista + getDirectorByMovieId(actualMovie.getId()),
                prefissoGenere + getMovieGenresById(actualMovie.getId()),
                prefissoDurata + getMovieRuntimeById(actualMovie.getId()),
                actualMovie.getVoteAverage() + "", prefissoEtaMinima +
                getAverageRecommendedAgeById(actualMovie.getId()), actualMovie.getPosterPath(),
                actualMovie.getOverview());
    }

    private void initializeAdapterForMovieCast(int id) {
        ArrayList<String> nomiCognomi = new ArrayList<>(),
                          nomiCognomiFilm = new ArrayList<>(),
                          castImagesUrl = new ArrayList<>();

        Thread t = new Thread(()-> {
            List<PersonCast> castList = tmdbMovies.getCredits(id).getCast();

            for(PersonCast person : castList) {
                nomiCognomi.add(person.getName());
                nomiCognomiFilm.add(person.getCharacter());
                castImagesUrl.add(person.getProfilePath());

                if(nomiCognomi.size() > 200) break;
            }

            if(castList.size() > 0) {
                ActorMovieAdapter actorMovieAdapter = new ActorMovieAdapter(showDetailsMovieActivity,
                        nomiCognomi, nomiCognomiFilm, castImagesUrl);

                actorMovieAdapter.setHasStableIds(true);

                showDetailsMovieActivity.setMovieCastRecyclerView(actorMovieAdapter);
            }
            else
                showDetailsMovieActivity.hideCastTextView();
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}
    }

    @NotNull
    private List<SlideModel> createListForBackgroundImage() {
        List<SlideModel> imageBackground = new ArrayList<>();
        int picturesCount = 0;

        Thread t = new Thread(()-> {
            if(tmdbMovies == null)
                tmdbMovies = new TmdbMovies(new TmdbApi(showDetailsMovieActivity.getResources().getString(R.string.themoviedb_api_key)));

            MovieImages images = tmdbMovies.getImages(actualMovie.getId(), null);
            actualMovie.setImages(images);
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        String firstPath = showDetailsMovieActivity.getResources().getString(R.string.first_path_image);
        for(Artwork currentImageBackground :actualMovie.getImages(ArtworkType.BACKDROP)){
            try {
                imageBackground.add(new SlideModel(firstPath + currentImageBackground.getFilePath(), ScaleTypes.CENTER_CROP));
                picturesCount++;
            }catch(NullPointerException ignore){}

            if(picturesCount == 12)
                break;
        }

        return imageBackground;
    }

    /* Restituisce la data di uscita italiana di un film in formato europeo,
       nel caso non disponibile prova a restituire la data di uscita americana */
    private String getEuropeanFormatMovieReleaseDate(@NotNull MovieDb movie) {
        String[] movieReleaseDate = new String[1];
        movieReleaseDate[0] = showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        Thread t = new Thread(()-> {
            String usFormatItalianReleaseDate = null;
            String usReleaseDate = null;
            //Recupero data di uscita in italia
            for(ReleaseInfo releaseInfo: tmdbMovies.getReleaseInfo(movie.getId(),"it")){
                if(releaseInfo.getCountry() != null &&
                    (releaseInfo.getCountry().equals("IT") || releaseInfo.getCountry().equals("it"))) {
                        for(ReleaseDate releaseDate: releaseInfo.getReleaseDates()) {
                            usFormatItalianReleaseDate = releaseDate.getReleaseDate().subSequence(0,10).toString();
                        }
                }
                else if(releaseInfo.getCountry() != null && releaseInfo.getCountry().equals("US")) {
                    for(ReleaseDate releaseDate: releaseInfo.getReleaseDates()) {
                        usReleaseDate = releaseDate.getReleaseDate().subSequence(0,10).toString();
                    }
                }
            }

            //Conversione formato data da americano a europeo
            if(usFormatItalianReleaseDate != null && !usFormatItalianReleaseDate.equals("")) {
                String[] arr = usFormatItalianReleaseDate.split("-");
                movieReleaseDate[0] = arr[2] + "-" + arr[1] + "-" + arr[0];
            }
            else if(usReleaseDate != null && !usReleaseDate.equals("")) {
                String[] arr = usReleaseDate.split("-");
                movieReleaseDate[0] = arr[2] + "-" + arr[1] + "-" + arr[0] + " [USA]";
            }
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return movieReleaseDate[0];
    }

    @NotNull
    private String getMovieRuntimeById(int id) {
        String[] runtime = new String[1];
        runtime[0] = showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        Thread t = new Thread(()-> {
            int time = tmdbMovies.getMovie(id,"it").getRuntime();
            if(time == 0)
                return;
            runtime[0] = time + "m";
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return runtime[0];
    }

    @NotNull
    private String getMovieGenresById(int id) {
        String[] trimmedGenres = new String[1];
        trimmedGenres[0] = showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        Thread t = new Thread(()-> {
            List<Genre> genres = tmdbMovies.getMovie(id, "it").getGenres();
            if(genres == null || genres.size() == 0)
                return;

            try {
                trimmedGenres[0] = genres.toString().replaceAll("\\[\\d+]|\\[|]", "");
                trimmedGenres[0] = trimmedGenres[0].replaceAll(" ,", ",");
            }catch(PatternSyntaxException ignore) {}
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return trimmedGenres[0];
    }

    private String getDirectorByMovieId(int id) {
        String[] director = new String[1];
        director[0] = showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        Thread t = new Thread(()-> {
            Credits credits = tmdbMovies.getCredits(id);
            if(credits != null) {
                for(PersonCrew person : credits.getCrew()) {
                    if(person.getJob().equals("Director"))
                        director[0] = person.getName();
                }
            }
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return director[0];
    }

    private String getAverageRecommendedAgeById(int id) {
        String[] certification = new String[1];
        certification[0] = showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        Thread t = new Thread(()-> {
            List<ReleaseInfo> releaseInfo = tmdbMovies.getReleaseInfo(id, "it");
            int size = 0, sum = 0;

            for(ReleaseInfo info: releaseInfo) {
                List<ReleaseDate> releaseDate = info.getReleaseDates();
                for(ReleaseDate rd: releaseDate) {
                    Scanner in = new Scanner(rd.getCertification()).useDelimiter("[^0-9]+");
                    try{
                        sum += in.nextInt();
                        size++;
                    }catch(NoSuchElementException ignore){}
                }
            }

            if(size > 0 && (sum/size) > 0)
                certification[0] = "" +  sum/size;
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore) {}

        return certification[0];
    }

    public void hideProgressBar() {
        if(activityParent != null) {
            if(activityParent instanceof HomeActivity)
                HomeController.getHomeControllerInstance().hideHomeMovieProgressBar();
            else if(activityParent instanceof SearchMovieActivity)
                SearchMovieController.getSearchMovieControllerInstance().hideSearchMovieProgressBar();
            else if(activityParent instanceof MoviesListActivity)
                MoviesListsController.getMoviesListControllerInstance().hideMoviesListProgressBar();
            else if(activityParent instanceof JoinedMoviesActivity)
                JoinedMoviesController.getJoinedMoviesControllerInstance().hideJoinedMoviesProgressBar();
        }
    }

    public boolean isSelectedMovieAlreadyInList(boolean isFavouritesList) {
        if(actualMovie == null) return false;

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        String email = User.getLoggedUser(showDetailsMovieActivity).getEmail();
        boolean[] contains = new boolean[1];

        Thread t = new Thread(()-> {
            String url;
            if(isFavouritesList)
                url = showDetailsMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/getPreferitiByPossessore/{FK_Possessore}";
            else
                url = showDetailsMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/getDaVedereByPossessore/{FK_Possessore}";

            ListaFilmDB listaFilm = restTemplate.getForObject(url, ListaFilmDB.class, email);

            url = showDetailsMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/containsFilm/{id}/{FK_Film}";

            contains[0] = restTemplate.getForObject(url, boolean.class, listaFilm.getId(), actualMovie.getId());
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        return contains[0];
    }


    //region Listener pulsanti per l'aggiunta/rimozione da una lista
    public View.OnClickListener getAggiungiPreferitiOnClickListener() {
        return v -> {
            if(actualMovie != null && showDetailsMovieActivity != null) {
                showDetailsMovieActivity.collapseFloatingActionMenu();
                showDetailsMovieActivity.temporarilyDisableFavouritesButton();
                getListenerForManageListOfFavourites("addFilmToListaFilm");
                showDetailsMovieActivity.changeAddFavouritesButtonToRemove();
            }
        };
    }

    public View.OnClickListener getRimuoviPreferitiOnClickListener() {
        return v -> {
            if(actualMovie != null && showDetailsMovieActivity != null) {
                showDetailsMovieActivity.temporarilyDisableFavouritesButton();
                showDetailsMovieActivity.collapseFloatingActionMenu();
                getListenerForManageListOfFavourites("removeFilmFromListaFilm");
                showDetailsMovieActivity.changeRemoveFavouritesButtonToAdd();
            }
        };
    }

    public View.OnClickListener getAggiungiDaVedereOnClickListener() {
        return v -> {
            if(actualMovie != null && showDetailsMovieActivity != null) {
                showDetailsMovieActivity.temporarilyDisableToWatchButton();
                showDetailsMovieActivity.collapseFloatingActionMenu();
                getListenerForManageListToWatch("addFilmToListaFilm");
                showDetailsMovieActivity.changeAddToWatchButtonToRemove();
            }
        };
    }

    public View.OnClickListener getRimuoviDaVedereOnClickListener() {
        return v -> {
            if(actualMovie != null && showDetailsMovieActivity != null) {
                showDetailsMovieActivity.temporarilyDisableToWatchButton();
                showDetailsMovieActivity.collapseFloatingActionMenu();
                getListenerForManageListToWatch("removeFilmFromListaFilm");
                showDetailsMovieActivity.changeRemoveToWatchButtonToAdd();
            }
        };
    }

    private void getListenerForManageListOfFavourites(String methodForEditingList) {
        getListenerForSendRequestsToTheServer("getPreferitiByPossessore", methodForEditingList);
    }

    private void getListenerForManageListToWatch(String methodForEditingList) {
        getListenerForSendRequestsToTheServer("getDaVedereByPossessore", methodForEditingList);
    }

    private void getListenerForSendRequestsToTheServer(String methodToRetrieveList, String methodForEditingList) {
        Thread t = new Thread(()-> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            String url = showDetailsMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/" + methodToRetrieveList + "/{FK_Possessore}";

            String email = User.getLoggedUser(showDetailsMovieActivity).getEmail();

            ListaFilmDB listaFilmPreferiti = restTemplate.getForObject(url, ListaFilmDB.class, email);
            HttpEntity<ListaFilmDB> requestListaPreferitiEntity = new HttpEntity<>(listaFilmPreferiti, headers);

            url = showDetailsMovieActivity.getResources().getString(R.string.db_path) + "ListaFilm/" + methodForEditingList + "/{FK_Film}";
            restTemplate.postForEntity(url, requestListaPreferitiEntity, ListaFilmDB.class, actualMovie.getId());
        });

        t.start();
        try {
            t.join();
        }catch (InterruptedException ignore) {}
    }

    public View.OnClickListener getYoutubeImageViewOnClickListener() {
        return v -> {
            if(actualMovieKeyTrailer != null) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + actualMovieKeyTrailer));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + actualMovieKeyTrailer));
                try {
                    showDetailsMovieActivity.startActivity(appIntent);
                    showDetailsMovieActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } catch (ActivityNotFoundException ex) {
                    showDetailsMovieActivity.startActivity(webIntent);
                    showDetailsMovieActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
    }

    public boolean isTrailerAvailable() {
        return actualMovieKeyTrailer != null;
    }

    public boolean isHomePageAvailable() {
        if(actualMovie == null) return false;

        if(actualMovie.getHomepage() == null || actualMovie.getHomepage().equals("")) {
            Thread t = new Thread(()-> {
                String homePage = tmdbMovies.getMovie(actualMovie.getId(), "it").getHomepage();
                if(homePage != null)
                    actualMovie.setHomepage(homePage);
            });
            t.start();

            try {
                t.join();
            }catch(InterruptedException ignore) {}

            return actualMovie.getHomepage() != null && !actualMovie.getHomepage().equals("");
        }
        return true;
    }

    public View.OnClickListener getHomePageOnClickListener() {
        return v -> {
            if(actualMovie.getHomepage() != null && !actualMovie.getHomepage().equals("")) {
                showDetailsMovieActivity.collapseFloatingActionMenu();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(actualMovie.getHomepage()));
                showDetailsMovieActivity.startActivity(browserIntent);
                showDetailsMovieActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };
    }
    //endregion
}
