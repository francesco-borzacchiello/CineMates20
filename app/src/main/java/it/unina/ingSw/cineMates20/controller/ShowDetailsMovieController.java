package it.unina.ingSw.cineMates20.controller;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import org.jetbrains.annotations.NotNull;

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
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import it.unina.ingSw.cineMates20.R;
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
    private AppCompatActivity activityParent;

    private ShowDetailsMovieController() {}

    public static ShowDetailsMovieController getShowDetailsMovieControllerInstance() {
        if(instance == null)
            instance = new ShowDetailsMovieController();
        return instance;
    }

    public void setShowDetailsMovieActivity(@NotNull ShowDetailsMovieActivity activity) {
        showDetailsMovieActivity = activity;
        hideProgressBar();

        Thread t = new Thread(()->
                tmdbMovies = new TmdbMovies(new TmdbApi(activity.getResources().getString(R.string.themoviedb_api_key))));
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}
    }

    public void initializeShowDetailsMovieActivity() {
        String title;
        if(actualMovie.getTitle() != null)
            title = actualMovie.getTitle();
        else
            title = actualMovie.getOriginalTitle();
        String prefissoDataDiUscita  = showDetailsMovieActivity.getResources().getString(R.string.data_di_uscita),
               prefissoRegista = showDetailsMovieActivity.getResources().getString(R.string.regista),
               prefissoGenere = showDetailsMovieActivity.getResources().getString(R.string.genere),
               prefissoDurata = showDetailsMovieActivity.getResources().getString(R.string.durata),
               prefissoEtaMinima = showDetailsMovieActivity.getResources().getString(R.string.etaConsigliata);

        showDetailsMovieActivity.setBackgroundImageSlider(createListForBackgroundImage());

        initializerAdapterForMovieCast(actualMovie.getId());

        showDetailsMovieActivity.setMovieDetails(title, prefissoDataDiUscita +
                getEuropeanFormatMovieReleaseDate(actualMovie),
                prefissoRegista + getDirectorByMovieId(actualMovie.getId()),
                prefissoGenere + getMovieGenresById(actualMovie.getId()),
                prefissoDurata + getMovieRuntimeById(actualMovie.getId()),
                actualMovie.getVoteAverage() + "", prefissoEtaMinima +
                getAverageRecommendedAgeById(actualMovie.getId()), actualMovie.getPosterPath(),
                actualMovie.getOverview());
    }

    private void initializerAdapterForMovieCast(int id) {
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
        }catch(InterruptedException ignore){ }
    }

    @NotNull
    private List<SlideModel> createListForBackgroundImage() {
        List<SlideModel> imageBackground = new ArrayList<>();
        int picturesCount = 0;

        Thread t = new Thread(()-> {
            MovieImages images = tmdbMovies.getImages(actualMovie.getId(), null);
            actualMovie.setImages(images);
        });
        t.start();

        try {
            t.join();
        }catch(InterruptedException ignore){}

        String firstPath = showDetailsMovieActivity.getResources().getString(R.string.first_path_backdrop_image);
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

    /* Restituisce la data di uscita italiana di un film in formato europeo, nel caso non disponibile
       prova a restituire la data di uscita americana */
    private String getEuropeanFormatMovieReleaseDate(@NotNull MovieDb movie) {
        //tmdbMovies.getReleaseInfo(movie.getId(),"it");
        String[] movieReleaseDate = new String[1];
        movieReleaseDate[0] = showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        Thread t = new Thread(()-> {
            String usFormatItalianReleaseDate = null;
            String usReleaseDate = null;
            //Recupero data di uscita in italia
            for(ReleaseInfo releaseInfo: tmdbMovies.getReleaseInfo(movie.getId(),"it")){
                if(releaseInfo.getCountry() != null && releaseInfo.getCountry().equals("IT")) {
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

    public void start(AppCompatActivity activityParent, MovieDb movie) {
        actualMovie = movie;
        Intent intent = new Intent(activityParent, ShowDetailsMovieActivity.class);
        //Utilizzato per comunicare a ShowDetailsMovieActivity se il suo chiamante non è SearchMovieActivity
        this.activityParent = activityParent;

        activityParent.startActivity(intent);
        activityParent.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void hideProgressBar() {
        if(activityParent != null) {
            if(activityParent instanceof HomeActivity)
                HomeController.getHomeControllerInstance().hideHomeMovieProgressBar();
            else if(activityParent instanceof SearchMovieActivity)
                SearchMovieController.getSearchMovieControllerInstance().hideSearchMovieProgressBar();
            else if(activityParent instanceof MoviesListActivity)
                MoviesListController.getMoviesListControllerInstance().hideMoviesListProgressBar();
            else if(activityParent instanceof JoinedMoviesActivity)
                JoinedMoviesController.getJoinedMoviesControllerInstance().hideJoinedMoviesProgressBar();
        }
    }
}
