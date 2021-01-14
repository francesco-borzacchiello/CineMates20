package it.unina.ingSw.cineMates20.controller;

import android.app.Activity;
import android.content.Intent;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import it.unina.ingSw.cineMates20.view.activity.ShowDetailsMovieActivity;
import it.unina.ingSw.cineMates20.view.adapter.ActorMovieAdapter;

public class ShowDetailsMovieController {

    private static ShowDetailsMovieController instance;
    private static TmdbMovies tmdbMovies;
    private ShowDetailsMovieActivity showDetailsMovieActivity;
    private MovieDb actualMovie;

    private ShowDetailsMovieController() {}

    public static ShowDetailsMovieController getShowDetailsMovieControllerInstance() {
        if(instance == null)
            instance = new ShowDetailsMovieController();
        return instance;
    }

    public void setShowDetailsMovieActivity(@NotNull ShowDetailsMovieActivity activity) {
        showDetailsMovieActivity = activity;
        tmdbMovies = new TmdbMovies(new TmdbApi(activity.getResources().getString(R.string.themoviedb_api_key)));
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

        List<PersonCast> castList = tmdbMovies.getCredits(id).getCast();
        for(PersonCast person : castList) {
            nomiCognomi.add(person.getName());
            nomiCognomiFilm.add(person.getCharacter());
            castImagesUrl.add(person.getProfilePath());
        }

        if(castList.size() > 0) {
            ActorMovieAdapter actorMovieAdapter = new ActorMovieAdapter(showDetailsMovieActivity,
                    nomiCognomi, nomiCognomiFilm, castImagesUrl);

            actorMovieAdapter.setHasStableIds(true);

            showDetailsMovieActivity.setMovieCastRecyclerView(actorMovieAdapter);
        }
        else
            showDetailsMovieActivity.hideCastTextView();
    }

    @NotNull
    private List<SlideModel> createListForBackgroundImage() {
        List<SlideModel> imageBackground = new ArrayList<>();
        int picturesCount = 0;

        MovieImages images = tmdbMovies.getImages(actualMovie.getId(), null);
        actualMovie.setImages(images);

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
        tmdbMovies.getReleaseInfo(movie.getId(),"it");
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
            return arr[2] + "-" + arr[1] + "-" + arr[0];
        }
        else if(usReleaseDate != null && !usReleaseDate.equals("")) {
            String[] arr = usReleaseDate.split("-");
            return arr[2] + "-" + arr[1] + "-" + arr[0] + " [USA]";
        }

        return showDetailsMovieActivity.getResources().getString(R.string.unavailable);
    }

    @NotNull
    private String getMovieRuntimeById(int id) {
        int runtime = tmdbMovies.getMovie(id,"it").getRuntime();
        if(runtime == 0)
            return showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        return runtime + "m";
    }

    @NotNull
    private String getMovieGenresById(int id) {
        List<Genre> genres = tmdbMovies.getMovie(id, "it").getGenres();
        if(genres == null || genres.size() == 0)
            return showDetailsMovieActivity.getResources().getString(R.string.unavailable);

        try {
            String trimmedGenres = genres.toString().replaceAll("\\[\\d+]|\\[|]", "");
            return trimmedGenres.replaceAll(" ,", ",");
        }catch(PatternSyntaxException e) {
            return showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        }
    }

    private String getDirectorByMovieId(int id) {
        String director = showDetailsMovieActivity.getResources().getString(R.string.unavailable);
        Credits credits = tmdbMovies.getCredits(id);
        if(credits != null) {
            for(PersonCrew person : credits.getCrew()) {
                if(person.getJob().equals("Director"))
                    director = person.getName();
            }
        }
        return director;
    }

    private String getAverageRecommendedAgeById(int id) {
        List<ReleaseInfo> releaseInfo = tmdbMovies.getReleaseInfo(id, "it");
        String certification = showDetailsMovieActivity.getResources().getString(R.string.unavailable);
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
            certification = "" +  sum/size;

        return certification;
    }

    public void start(Activity activityParent, MovieDb movie, @Nullable String caller) {
        actualMovie = movie;
        Intent intent = new Intent(activityParent, ShowDetailsMovieActivity.class);

        //Utilizzato per comunicare a ShowDetailsMovieActivity se il suo chiamante non è SearchMovieActivity
        if(caller != null)
            intent.putExtra("caller", caller);

        activityParent.startActivity(intent);
    }

    public void hideSearchMovieProgressBar() {
        SearchMovieController.getSearchMovieControllerInstance().hideSearchMovieProgressBar();
    }

    public void hideHomeMovieProgressBar() {
        HomeController.getHomeControllerInstance().hideHomeMovieProgressBar();
    }
}
